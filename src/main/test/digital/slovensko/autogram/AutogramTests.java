package digital.slovensko.autogram;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AutogramTests {
    @Test
    void testSignViaServer() {
        responder = mock(Responder.class);

        var autogram = new Autogram(responder);
        autogram.sign(new SignDocumentRequest());

        verify(responder).onDocumentSigned(argThat( r -> {
            assertThat(r).isNotNull();
        }));
    }
}