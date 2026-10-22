package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.DriverDetector;
import digital.slovensko.autogram.core.LegacyInstallation;
import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningMode;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Wires up an {@link Autogram} instance that signs as {@link TestPki#JANA} with timestamps from the test TSA
 * ({@link TestPki#shared()}), so that tests outside the core package can drive real signing without any UI,
 * hardware token or network.
 */
public class TestAutogramFactory {
    public static Autogram create(UI ui) {
        return new Autogram(ui, new FakeSettings());
    }

    public static Autogram create() {
        return create(new FakeUI());
    }

    private static class FakeSettings extends UserSettings {
        @Override
        public DriverDetector getDriverDetector() {
            return () -> List.of(new FakeTokenDriver());
        }

        @Override
        public TSPSource getTspSource() {
            return TestPki.shared().tspSource();
        }
    }

    private static class FakeTokenDriver extends TokenDriver {
        FakeTokenDriver() {
            super("fake", Path.of(""), "fake", "");
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
            return TestPki.shared().signer(TestPki.JANA).token();
        }
    }

    public static class FakeUI implements UI {
        @Override
        public void selectBatchMode(Batch batch, Consumer<SigningMode> onSelected, Runnable onCancel) {
            onSelected.accept(SigningMode.BULK);
        }

        @Override
        public void startSigning(SigningJob job, Autogram autogram) {
        }

        @Override
        public void startBatch(Batch batch, Autogram autogram) {
        }

        @Override
        public void closeBatch() {
        }

        @Override
        public void showSigningJob(SigningJob job, Autogram autogram) {
        }

        @Override
        public void closeSigningJob(SigningJob job) {
        }

        @Override
        public void onTokenSearchStarted() {
        }

        @Override
        public void pickTokenAndThen(TokenOptions options, Supplier<TokenOptions> searchAgain, Consumer<TokenOption> callback, Runnable onCancel) {
            if (!options.found().isEmpty())
                callback.accept(options.found().get(0));
            else
                callback.accept(new TokenOption(options.otherDrivers().get(0), null));
        }

        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback, Runnable onCancel) {
            callback.accept(keys.get(0));
        }

        @Override
        public void onPickSigningKeyFailed(AutogramException e) {
            throw e;
        }

        @Override
        public void onSigningSuccess(SigningJob job) {
        }

        @Override
        public void enableSigningOnAllJobs() {
        }

        @Override
        public void onSigningFailed(AutogramException e, SigningJob job) {
            throw e;
        }

        @Override
        public void onSigningFailed(AutogramException e) {
            throw e;
        }

        @Override
        public void onDocumentSaved(File targetFile) {
        }

        @Override
        public void onDocumentBatchSaved(BatchUiResult result) {
        }

        @Override
        public void onWorkThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUIThreadDo(Runnable callback) {
            callback.run();
        }

        @Override
        public void onUpdateAvailable() {
        }

        @Override
        public void onLegacyInstallationFound(List<LegacyInstallation> installations) {
        }

        @Override
        public void onAboutInfo() {
        }

        @Override
        public void onPDFAComplianceCheckFailed(SigningJob job) {
        }

        @Override
        public void onSignatureValidationCompleted(ValidationReports reports) {
        }

        @Override
        public void onSignatureCheckCompleted(ValidationReports reports) {
        }

        @Override
        public void showIgnorableExceptionDialog(IgnorableException exception) {
        }

        @Override
        public void showError(AutogramException exception) {
        }

        @Override
        public char[] getKeystorePassword() {
            return null;
        }

        @Override
        public char[] getContextSpecificPassword(boolean incorrectPIN, boolean canReturnToSigning) {
            return null;
        }

        @Override
        public void updateBatch() {
        }

        @Override
        public void resetSigningKey() {
        }

        @Override
        public void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel) {
            callback.accept(() -> {
            });
        }
    }
}
