package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

public interface UI {
    void startSigning(SigningJob job, Autogram autogram);

    void startBatch(Batch batch, Autogram autogram, BatchStartCallback callback);

    void cancelBatch(Batch batch);

    void showVisualization(Visualization visualization, Autogram autogram);

    void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback, Runnable onCancel);

    void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback);

    void onPickSigningKeyFailed(AutogramException e);

    void onSigningSuccess(SigningJob job);

    void onSigningFailed(AutogramException e, SigningJob job);

    void onSigningFailed(AutogramException e);

    void onDocumentSaved(File targetFile);

    void onDocumentBatchSaved(BatchUiResult result);

    void onWorkThreadDo(Runnable callback);

    void onUIThreadDo(Runnable callback);

    void onUpdateAvailable();

    void onAboutInfo();

    void onPDFAComplianceCheckFailed(SigningJob job);

    void onSignatureValidationCompleted(ValidationReports reports);

    void onSignatureCheckCompleted(ValidationReports reports);

    void showIgnorableExceptionDialog(IgnorableException exception);

    void showError(AutogramException exception);

    char[] getKeystorePassword();

    char[] getContextSpecificPassword();

    public void updateBatch();

    void resetSigningKey();

    void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel);
}
