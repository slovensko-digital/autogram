package digital.slovensko.autogram.server;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.ui.SupportedLanguage;
import eu.europa.esig.dss.model.DSSDocument;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * A request or response too large for the available memory: the client gets an error response instead of an
 * OutOfMemoryError that ends the server thread and leaves the connection open.
 */
class ApiOutOfMemoryTest {
    private final ByteArrayOutputStream response = new ByteArrayOutputStream();

    @BeforeAll
    static void initErrorResponses() {
        ErrorResponseBuilder.init(SupportedLanguage.ENGLISH.loadResources());
    }

    @Test
    void versionedSignEndpointRespondsWhenRequestIsTooLarge() throws IOException {
        assertRespondsWithNotEnoughMemory(new VersionedSignEndpoint(mock(Autogram.class)), "POST");
    }

    @Test
    void legacySignEndpointRespondsWhenRequestIsTooLarge() throws IOException {
        assertRespondsWithNotEnoughMemory(new SignEndpoint(mock(Autogram.class)), "POST");
    }

    @Test
    void batchEndpointRespondsWhenRequestIsTooLarge() throws IOException {
        assertRespondsWithNotEnoughMemory(new BatchEndpoint(mock(Autogram.class)), "POST");
    }

    /** The signed document is too large to encode into the response, the client gets an error instead of 200. */
    @Test
    void responderRespondsWhenSignedDocumentIsTooLarge() throws IOException {
        var exchange = exchange(InputStream.nullInputStream(), "POST");
        var signedDocument = mock(SignedDocument.class, RETURNS_DEEP_STUBS);
        var document = mock(DSSDocument.class);
        when(document.openStream()).thenThrow(new OutOfMemoryError("Java heap space"));
        when(signedDocument.getDocument()).thenReturn(document);

        new ServerResponder(exchange).onDocumentSigned(signedDocument);

        verify(exchange, never()).sendResponseHeaders(eq(200), anyLong());
        assertNotEnoughMemoryResponse(exchange);
    }

    private void assertRespondsWithNotEnoughMemory(HttpHandler endpoint, String method) throws IOException {
        var body = new InputStream() {
            @Override
            public int read() {
                throw new OutOfMemoryError("Java heap space");
            }
        };
        var exchange = exchange(body, method);

        endpoint.handle(exchange);

        assertNotEnoughMemoryResponse(exchange);
    }

    private void assertNotEnoughMemoryResponse(HttpExchange exchange) throws IOException {
        verify(exchange).sendResponseHeaders(eq(500), anyLong());
        var json = response.toString(StandardCharsets.UTF_8);
        assertTrue(json.contains("\"code\":\"INTERNAL_ERROR\""), json);
        assertTrue(json.contains("\"message\":\"Not enough memory\""), json);
    }

    private HttpExchange exchange(InputStream requestBody, String method) {
        var exchange = mock(HttpExchange.class);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getRequestBody()).thenReturn(requestBody);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(response);
        return exchange;
    }
}
