package digital.slovensko.autogram;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.DriverDetector;
import digital.slovensko.autogram.core.PasswordManager;
import digital.slovensko.autogram.core.SignatureTokenSettings;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.SigningMode;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Wires up an {@link Autogram} instance backed by the test PKCS12 keystore
 * (src/test/resources/digital/slovensko/autogram/core/test.keystore) so that
 * tests outside the core package can drive real signing without any UI or
 * hardware token.
 */
public class TestAutogramFactory {
    private static final String TEST_KEYSTORE_RESOURCE = "/digital/slovensko/autogram/core/test.keystore";
    private static final String TEST_TSA_URL = "https://freetsa.org/tsr";

    public static Autogram create(UI ui) {
        var settings = new FakeSettings();
        settings.setTsaServer(TEST_TSA_URL);
        return new Autogram(ui, settings);
    }

    public static Autogram create() {
        return create(new FakeUI());
    }

    private static class FakeSettings extends UserSettings {
        @Override
        public DriverDetector getDriverDetector() {
            return () -> List.of(new FakeTokenDriver());
        }
    }

    private static class FakeTokenDriver extends TokenDriver {
        FakeTokenDriver() {
            super("fake", Path.of(""), "fake", "");
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken(PasswordManager pm, SignatureTokenSettings settings) {
            try {
                var keystore = Objects.requireNonNull(getClass().getResource(TEST_KEYSTORE_RESOURCE)).getFile();
                return new Pkcs12SignatureToken(keystore, new KeyStore.PasswordProtection("".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public static class FakeUI implements UI {
        @Override
        public void startSigning(SigningJob job, Autogram autogram) {
        }

        @Override
        public void startBatch(Batch batch, Autogram autogram, Consumer<SigningKey> onKeySelected, Runnable onCancel) {
        }

        @Override
        public void selectBatchMode(Batch batch, Consumer<SigningMode> onSelected, Runnable onCancel) {
            onSelected.accept(SigningMode.BULK);
        }

        @Override
        public void cancelBatch(Batch batch) {
        }

        @Override
        public void showSigningJob(SigningJob job, Autogram autogram) {
        }

        @Override
        public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback, Runnable onCancel) {
            callback.accept(drivers.get(0));
        }

        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback) {
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
        public char[] getContextSpecificPassword(AutogramException previousError) {
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
