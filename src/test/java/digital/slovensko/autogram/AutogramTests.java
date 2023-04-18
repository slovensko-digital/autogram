package digital.slovensko.autogram;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.security.KeyStore;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutogramTests {
    @Test
    void testSignHappyScenario() {
        var ui = new DummyUIPickingFakeTestTokenDriver();

        var parameters = new SigningParameters();
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

        ui.showSigningDialog(new SigningJob(document, parameters, responder));

        verify(responder).onDocumentSigned(any());
    }

    @Test
    void testSignCertificatePickFailed() {

    }

    @Test
    void testSignFailedAfterCertificatePick() {

    }

    private class DummyUIPickingFakeTestTokenDriver implements UI {
        @Override
        public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
            callback.call(keys.get(0));
        }

        @Override
        public void hideSigningDialog(SigningJob job) {

        }

        @Override
        public void showPickFileDialog() {

        }

        @Override
        public void showSigningDialog(SigningJob job) {
            getKeyAndThen(job::signAndRespond);
        }

        private void getKeyAndThen(SigningKeyLambda callback) {
            pickTokenDriverAndThen(getAvailableDrivers(), driver -> {
                var token = driver.createTokenWithPassword(null);
                var keys = token.getKeys();
                pickKeyAndThen(keys, (privateKey) -> {
                    callback.call(new SigningKey(token, privateKey));
                });
            });
        }

        private List<TokenDriver> getAvailableDrivers() {
            return List.of(new FakeTokenDriver("fake"));
        }

        @Override
        public void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback) {
            callback.call(new FakeTokenDriver("fake"));
        }


        @Override
        public void refreshSigningKey() {
        }

        @Override
        public void showError(AutogramException e) {
        }

        @Override
        public void showPasswordDialogAndThen(TokenDriver driver, PasswordLambda callback) {
            callback.call(null);
        }

        public void sign(SigningJob signingJob) {

        }
    }

    private static class FakeTokenDriver extends TokenDriver {
        public FakeTokenDriver(String name) {
            super(name, Path.of(""), true);
        }

        @Override
        public AbstractKeyStoreTokenConnection createTokenWithPassword(char[] password) {
            try {
                return new Pkcs12SignatureToken(new File("test.keystore"), new KeyStore.PasswordProtection("".toCharArray()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}