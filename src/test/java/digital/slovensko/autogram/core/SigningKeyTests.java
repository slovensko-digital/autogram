package digital.slovensko.autogram.core;

import eu.europa.esig.dss.token.AbstractKeyStoreTokenConnection;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import org.junit.jupiter.api.Test;

import java.security.ProviderException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SigningKeyTests {
    @Test
    void testClosingKeyOfRemovedCardDoesNotFail() {
        var token = mock(AbstractKeyStoreTokenConnection.class);
        doThrow(new ProviderException("Token has been removed")).when(token).close();
        var key = new SigningKey(token, mock(DSSPrivateKeyEntry.class));

        assertDoesNotThrow(key::close);
        verify(token).close();
    }
}
