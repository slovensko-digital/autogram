package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.server.dto.Document;
import digital.slovensko.autogram.server.errors.EmptyBodyException;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Reading a JSON request body: parsed while it is being read, errors reported the same as before. */
class EndpointUtilsTest {

    /** The body is never read whole into memory, the stream fails if it is. */
    @Test
    void parsesBodyWhileReadingIt() throws IOException {
        var body = new ByteArrayInputStream("""
                {"filename": "zmluva-ľščťžýáíé.txt", "content": "obsah"}
                """.getBytes(StandardCharsets.UTF_8)) {
            @Override
            public synchronized byte[] readAllBytes() {
                throw new AssertionError("the whole body was read into memory");
            }
        };

        var document = EndpointUtils.loadFromJsonExchange(exchangeWithBody(body), Document.class);

        assertEquals("zmluva-ľščťžýáíé.txt", document.filename());
        assertEquals("obsah", document.content());
    }

    @Test
    void emptyBodyIsReported() {
        assertThrows(EmptyBodyException.class, () -> load(""));
    }

    @Test
    void blankBodyIsNotParsed() {
        var error = assertThrows(IOException.class, () -> load("  \n"));

        assertEquals("Failed to parse JSON body", error.getMessage());
    }

    @Test
    void incompleteJsonIsReported() {
        assertThrows(JsonSyntaxException.class, () -> load("{\"filename\": \"zmluva.txt\""));
    }

    @Test
    void textAfterJsonIsReported() {
        assertThrows(JsonSyntaxException.class, () -> load("{\"filename\": \"zmluva.txt\"} unexpected-text"));
    }

    /** E.g. the client closes the connection while sending the body, Gson reports it as invalid JSON. */
    @Test
    void failedReadingIsReported() {
        var body = new InputStream() {
            private int read;

            @Override
            public int read() throws IOException {
                if (read++ < 10)
                    return '{';
                throw new IOException("Connection reset");
            }
        };

        assertThrows(JsonSyntaxException.class, () -> EndpointUtils.loadFromJsonExchange(exchangeWithBody(body), Document.class));
    }

    private static Document load(String body) throws IOException {
        var stream = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
        return EndpointUtils.loadFromJsonExchange(exchangeWithBody(stream), Document.class);
    }

    private static HttpExchange exchangeWithBody(InputStream body) {
        var exchange = mock(HttpExchange.class);
        when(exchange.getRequestBody()).thenReturn(body);
        return exchange;
    }
}
