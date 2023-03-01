package digital.slovensko.autogram;

import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import eu.europa.esig.dss.token.Pkcs12SignatureToken;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.security.KeyStore;
import java.util.List;
import java.util.Objects;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class AutogramTests {
    @Test
    void testSignHappyScenario() {
        var autogram = new Autogram(new DummyUIPickingFakeTestTokenDriver());

        var parameters = new SigningParameters();
        var document = new FileDocument("pom.xml");
        var responder = mock(Responder.class);

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
        public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
            callback.call(keys.get(0));
        }

        @Override
        public void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback) {
            callback.call(new FakeTokenDriver());
        }

        @Override
        public void showSigningDialog(SigningJob job, Autogram autogram) {
        }

        @Override
        public void refreshSigningKey(SigningKey key) {
        }
    }

    private class FakeTokenDriver extends TokenDriver {
        @Override
        public AbstractKeyStoreTokenConnection createToken() throws IOException {
            return new Pkcs12SignatureToken(new File("test.keystore"), new KeyStore.PasswordProtection("".toCharArray()));
        }
    }
}