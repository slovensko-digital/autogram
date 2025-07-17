package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchStartCallback;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.core.errors.NoValidKeysDetectedException;
import digital.slovensko.autogram.core.errors.PkcsEidWindowsDllException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.TokenRemovedException;
import digital.slovensko.autogram.core.errors.UnrecognizedException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.geometry.Rectangle2D;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.function.Consumer;

public class GUI implements UI {
    private static final Logger LOGGER = LoggerFactory.getLogger(GUI.class);

    private final Map<SigningJob, SigningDialogController> jobControllers = new WeakHashMap<>();
    private SigningKey activeKey;
    private boolean driverWasAlreadySet = false;
    private final HostServices hostServices;
    private final UserSettings userSettings;
    private BatchDialogController batchController;
    private static final boolean DEBUG = false;
    private int nWindows = 0;

    public GUI(HostServices hostServices, UserSettings userSettings) {
        this.hostServices = hostServices;
        this.userSettings = userSettings;
    }

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        autogram.startVisualization(job);
    }

    @Override
    public void startBatch(Batch batch, Autogram autogram, BatchStartCallback callback) {
        batchController = new BatchDialogController(batch, callback, autogram, this);
        var root = GUIUtils.loadFXML(batchController, "batch-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(batchController.i18n("batch.title"));
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> {
            cancelBatch(batch);
            callback.cancel();
        });

        stage.setResizable(false);
        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, batchController);
        GUIUtils.showOnTop(stage);
        setUserFriendlyPositionAndLimits(stage);
    }

    @Override
    public void cancelBatch(Batch batch) {
        batchController.close();
        batch.end();
        refreshKeyOnAllJobs();
        enableSigningOnAllJobs();
    }

    public void updateBatch() {
        if (batchController == null)
            return;

        batchController.update();
    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback, Runnable onCancel) {
        disableKeyPicking();

        if (drivers.isEmpty()) {
            showError(new NoDriversDetectedException());
            refreshKeyOnAllJobs();
            enableSigningOnAllJobs();
        } else if (drivers.size() == 1) {
            // short-circuit if only one driver present
            callback.accept(drivers.get(0));
        } else {
            if (!driverWasAlreadySet && userSettings.getDefaultDriver() != null) {
                try {
                    driverWasAlreadySet = true;
                    var defaultDriver = drivers.stream().filter(d -> d.getName().equals(userSettings.getDefaultDriver()))
                            .findFirst().get();

                    if (defaultDriver != null) {
                        callback.accept(defaultDriver);
                        return;
                    }
                } catch (NoSuchElementException e) {
                }
            }

            PickDriverDialogController controller = new PickDriverDialogController(drivers, callback);
            var root = GUIUtils.loadFXML(controller, "pick-driver-dialog.fxml");

            var stage = new Stage();
            stage.setTitle(controller.i18n("pickDriver.title"));
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                refreshKeyOnAllJobs();
                enableSigningOnAllJobs();
                if (onCancel != null)
                    onCancel.run();
            });
            stage.sizeToScene();
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.show();
        }
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException(driver.getNoKeysHelperText()));
            refreshKeyOnAllJobs();
            enableSigningOnAllJobs();

            return;
        }

        var keysStream = keys.stream();
//        TODO: NFC eID returns false for qualified certificate #367
//        var keysStream = keys.stream().filter(k -> k.getCertificate().checkKeyUsage(KeyUsageBit.DIGITAL_SIGNATURE));
        if (!userSettings.isExpiredCertsEnabled()) {
            var now = new Date();
            keysStream = keysStream.filter(k -> k.getCertificate().isValidOn(now));
        }

        keys = keysStream.toList();
        if (keys.isEmpty()) {
            showError(new NoValidKeysDetectedException());
            refreshKeyOnAllJobs();
            enableSigningOnAllJobs();

            return;
        }

        var controller = new PickKeyDialogController(keys, callback, userSettings.isExpiredCertsEnabled());
        var root = GUIUtils.loadFXML(controller, "pick-key-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("pickKey.title"));
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> {
            refreshKeyOnAllJobs();
            enableSigningOnAllJobs();
        });
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.show();
    }

    public void refreshKeyOnAllJobs() {
        jobControllers.values().forEach(SigningDialogController::refreshSigningKey);
        if (batchController != null) {
            batchController.refreshSigningKey();
        }
    }

    public void enableSigningOnAllJobs() {
        jobControllers.values().forEach(SigningDialogController::enableSigning);
        if (batchController != null)
            batchController.enableSigning();
    }

    @Override
    public void showError(AutogramException e) {
        GUIUtils.showError(e, "general.continue.btn", false);
    }

    public void showPkcsEidWindowsDllError(AutogramException e) {
        var controller = new PkcsEidWindowsDllErrorController(hostServices);
        var root = GUIUtils.loadFXML(controller, "pkcs-eid-windows-dll-error-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(e.getSubheading(controller.resources));
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        GUIUtils.suppressDefaultFocus(stage, controller);

        stage.show();
    }

    public char[] getKeystorePassword() {
        var futurePassword = new FutureTask<>(() -> {
            var controller = new PasswordController("password.keystore.text", "password.keystore.error.text", false, true);
            var root = GUIUtils.loadFXML(controller, "password-dialog.fxml");

            var stage = new Stage();
            stage.setTitle(controller.i18n("password.keystore.title"));
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                refreshKeyOnAllJobs();
                enableSigningOnAllJobs();
            });
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            return controller.getPassword();
        });

        Platform.runLater(futurePassword);

        try {
            return futurePassword.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }


    public char[] getContextSpecificPassword() {
        var futurePassword = new FutureTask<>(() -> {
            var controller = new PasswordController("password.context.text", "password.context.error.text", true, false);
            var root = GUIUtils.loadFXML(controller, "password-dialog.fxml");

            var stage = new Stage();
            stage.setTitle(controller.i18n("password.context.title"));
            stage.setScene(new Scene(root));
            stage.setOnCloseRequest(e -> {
                refreshKeyOnAllJobs();
                enableSigningOnAllJobs();
            });
            stage.setResizable(false);
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            return controller.getPassword();
        });

        Platform.runLater(futurePassword);

        try {
            return futurePassword.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onUpdateAvailable() {
        var controller = new UpdateController(hostServices);
        var root = GUIUtils.loadFXML(controller, "update-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("update.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onAboutInfo() {
        var controller = new AboutDialogController(hostServices);
        var root = GUIUtils.loadFXML(controller, "about-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("about.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onPDFAComplianceCheckFailed(SigningJob job) {
        var controller = new PDFAComplianceDialogController(job, this);
        var root = GUIUtils.loadFXML(controller, "pdfa-compliance-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("pdfaCompliance.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getJobWindow(job));
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onSignatureValidationCompleted(ValidationReports reports) {
        var controller = jobControllers.get(reports.getSigningJob());
        controller.onSignatureValidationCompleted(reports.getReports());
    }

    @Override
    public void onSignatureCheckCompleted(ValidationReports reports) {
        var controller = jobControllers.get(reports.getSigningJob());
        controller.onSignatureCheckCompleted(reports.haveSignatures() ? reports.getReports() : null);
    }

    public void showVisualization(Visualization visualization, Autogram autogram) {
        var title = "Dokument";
        if (visualization.getJob().getDocument().getName() != null)
            title = "Dokument " + visualization.getJob().getDocument().getName();

        var controller = new SigningDialogController(visualization, autogram, this, title, userSettings.isSignaturesValidity());
        jobControllers.put(visualization.getJob(), controller);

        Parent root;
        try {
            root = GUIUtils.loadFXML(controller, "signing-dialog.fxml");
        } catch (AutogramException e) {
            showError(e);
            return;
        } catch (Exception e) {
            showError(new UnrecognizedException(e));
            return;
        }
        var stage = new Stage();
        stage.setTitle(title);
        stage.setScene(new Scene(root));
        stage.setOnCloseRequest(e -> cancelJob(visualization.getJob()));

        stage.sizeToScene();

        GUIUtils.suppressDefaultFocus(stage, controller);
        GUIUtils.showOnTop(stage);
        GUIUtils.hackToForceRelayout(stage);
        setUserFriendlyPositionAndLimits(stage);

        onWorkThreadDo(()
                -> autogram.checkAndValidateSignatures(visualization.getJob()));
    }

    @Override
    public void showIgnorableExceptionDialog(IgnorableException e) {
        var controller = new IgnorableExceptionDialogController(e);
        var root = GUIUtils.loadFXML(controller, "ignorable-exception-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("error.ignorable.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(getJobWindow(e.getJob()));
        GUIUtils.suppressDefaultFocus(stage, controller);

        GUIUtils.showOnTop(stage);
    }

    private void disableKeyPicking() {
        jobControllers.values().forEach(SigningDialogController::disableKeyPicking);
        if (batchController != null)
            batchController.disableKeyPicking();
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        if (e instanceof PkcsEidWindowsDllException)
            showPkcsEidWindowsDllError(e);
        else
            showError(e);

        resetSigningKey();
        enableSigningOnAllJobs();
    }

    @Override
    public void onSigningSuccess(SigningJob job) {
        jobControllers.get(job).close();
        refreshKeyOnAllJobs();
        enableSigningOnAllJobs();
        updateBatch();
    }

    @Override
    public void onSigningFailed(AutogramException e, SigningJob job) {
        var controller = jobControllers.get(job);
        controller.close();
        jobControllers.remove(job);
        onSigningFailed(e);
    }

    @Override
    public void onSigningFailed(AutogramException e) {
        showError(e);
        if (e instanceof TokenRemovedException) {
            resetSigningKey();
        } else {
            refreshKeyOnAllJobs();
        }
        enableSigningOnAllJobs();
    }

    @Override
    public void onDocumentSaved(File targetFile) {
        var controller = new SigningSuccessDialogController(targetFile, hostServices);
        var root = GUIUtils.loadFXML(controller, "signing-success-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("signing.success.title"));
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();
    }

    @Override
    public void onDocumentBatchSaved(BatchUiResult result) {
        var stage = new Stage();
        SuppressedFocusController controller;
        Parent root;
        if (result.hasErrors()) {
            controller = new BatchSigningFailureDialogController(result, hostServices);
            root = GUIUtils.loadFXML(controller, "batch-signing-failure-dialog.fxml");
            stage.setTitle(((BatchSigningFailureDialogController) controller).i18n("batchSigning.failure.title"));
        } else {
            controller = new BatchSigningSuccessDialogController(result, hostServices);
            root = GUIUtils.loadFXML(controller, "batch-signing-success-dialog.fxml");
            stage.setTitle(((BatchSigningSuccessDialogController) controller).i18n("batchSigning.success.title"));
        }
        stage.setScene(new Scene(root));
        stage.setResizable(false);
        stage.sizeToScene();
        GUIUtils.suppressDefaultFocus(stage, controller);
        stage.show();

        enableSigningOnAllJobs();
    }

    @Override
    public void onWorkThreadDo(Runnable callback) {
        if (Platform.isFxApplicationThread()) {
            new Thread(callback).start();
        } else {
            callback.run();
        }
    }

    @Override
    public void onUIThreadDo(Runnable callback) {
        if (Platform.isFxApplicationThread()) {
            callback.run();
        } else {
            Platform.runLater(callback);
        }
    }

    public SigningKey getActiveSigningKey() {
        return activeKey;
    }

    public void setActiveSigningKeyAndThen(SigningKey newKey, Consumer<SigningKey> callback) {
        if (!isActiveSigningKeyChangeAllowed())
            throw new RuntimeException("Signing key change is not allowed");

        if (activeKey != null)
            activeKey.close();

        activeKey = newKey;
        driverWasAlreadySet = true;
        refreshKeyOnAllJobs();

        if (callback != null)
            callback.accept(newKey);
        else
            enableSigningOnAllJobs();
    }

    public boolean isActiveSigningKeyChangeAllowed() {
        return true;
    }

    public void disableSigning() {
        jobControllers.values().forEach(SigningDialogController::disableSigning);
        if (batchController != null)
            batchController.disableSigning();
    }

    @Override
    public void resetSigningKey() {
        onUIThreadDo(()->{
            setActiveSigningKeyAndThen(null, null);
            refreshKeyOnAllJobs();
        });
    }

    public void cancelJob(SigningJob job) {
        job.onDocumentSignFailed(new SigningCanceledByUserException());
        jobControllers.get(job).close();
    }

    public void focusJob(SigningJob job) {
        getJobWindow(job).requestFocus();
    }

    private Window getJobWindow(SigningJob job) {
        return jobControllers.get(job).mainBox.getScene().getWindow();
    }

    private void setUserFriendlyPositionAndLimits(Stage stage) {
        var maxWindows = 10;
        var maxOffset = 25;
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        var sceneWidth = stage.getScene().getWidth();
        var availabeWidth = (bounds.getWidth() - sceneWidth);
        var singleOffsetXPx = Math.round(Math.min(maxOffset, (availabeWidth / 2) / maxWindows)); // spread windows into
        // half of availabe
        // screen width
        var offsetX = singleOffsetXPx * (nWindows - maxWindows / 2);
        double idealX = bounds.getMinX() + availabeWidth / 2 + offsetX;
        double x = Math.max(bounds.getMinX(), Math.min(bounds.getMaxX() - sceneWidth, idealX));
        var sceneHeight = stage.getScene().getHeight();
        double y = Math.max(bounds.getMinY(),
                Math.min(bounds.getMaxY() - sceneHeight, bounds.getMinY() + (bounds.getHeight() - sceneHeight) / 2));
        stage.setX(x);
        stage.setY(y);
        stage.setMaxHeight(bounds.getHeight());
        stage.setMaxWidth(bounds.getWidth());
        nWindows = (nWindows + 1) % maxWindows;
    }

    @Override
    public void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel) {
        var controller = new ConsentCertificateReadingDialogController(hostServices, callback, onCancel);
        var root = GUIUtils.loadFXML(controller, "consent-certificate-reading-dialog.fxml");

        var stage = new Stage();
        stage.setTitle(controller.i18n("certConsent.title"));
        stage.setScene(new Scene(root));

        stage.sizeToScene();
        stage.setResizable(false);
        stage.initModality(Modality.APPLICATION_MODAL);

        stage.setOnCloseRequest(e -> {
            if (onCancel != null)
                onCancel.run();
        });

        GUIUtils.suppressDefaultFocus(stage, controller);

        stage.show();
    }
}
