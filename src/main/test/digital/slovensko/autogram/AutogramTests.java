package digital.slovensko.autogram;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutogramTests {
    @Test
    void testSignHappyScenario() {
        var autogram = new Autogram(new DummyUIPickingFakeTestTokenDriver());

        var parameters = new SigningParameters();
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

        autogram.pickSigningKey();
        autogram.sign(new SigningJob(document, parameters, responder));

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
        public void start(Autogram autogram, String[] args) {

        }

        @Override
        public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
            callback.call(keys.get(0));
        }

        @Override
        public void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback) {
            callback.call(new FakeTokenDriver("fake"));
        }

        @Override
        public void showSigningDialog(SigningJob job, Autogram autogram) {
        }

        @Override
        public void hideSigningDialog(SigningJob job, Autogram autogram) {

        }

        @Override
        public void refreshSigningKey() {
        }
    }

    private class FakeTokenDriver extends TokenDriver {
        public FakeTokenDriver(String name) {
            super(name);
        }

        @Override
        public AbstractKeyStoreTokenConnection createToken() throws IOException {
            return new Pkcs12SignatureToken(new File("test.keystore"), new KeyStore.PasswordProtection("".toCharArray()));
        }
    }
}