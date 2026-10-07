package digital.slovensko.autogram.ui;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface UI {
    void startSigning(SigningJob job, Autogram autogram);

    void startBatch(Batch batch, Autogram autogram, BatchStartCallback callback);

    void cancelBatch(Batch batch);

    void showSigningJob(SigningJob job, Autogram autogram);

    /**
     * Called before searching for tokens (cards) of all drivers, which may take a while.
     */
    void onTokenSearchStarted();

    /**
     * Lets the user pick a token (card) or a driver to sign with.
     *
     * @param searchAgain searches for tokens again, e.g. after the user inserted a card - call it on work thread
     */
    void pickTokenAndThen(TokenOptions options, Supplier<TokenOptions> searchAgain, Consumer<TokenOption> callback, Runnable onCancel);

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
