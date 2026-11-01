package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.EmptyBodyException;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PushbackInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class EndpointUtils {
    private final static Gson gson = new Gson();

    public static void respondWithError(ErrorResponse error, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(error.getStatusCode(), 0);
            if (error.getStatusCode() != 204)
                exchange.getResponseBody().write(gson.toJson(error.getBody()).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new ResponseNetworkErrorException(e);
        }
    }

    public static void respondWith(Object response, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new ResponseNetworkErrorException(e);
        }
    }

    /**
     * Parses the body while it is being read, a copy of the whole body as bytes and another one as a string would
     * need more than twice the size of the documents in it.
     */
    public static <T> T loadFromJsonExchange(HttpExchange exchange, Class<T> classOfT) throws IOException {
        var body = new PushbackInputStream(exchange.getRequestBody());
        var first = body.read();
        if (first == -1)
            throw new EmptyBodyException();
        body.unread(first);

        var ret = gson.fromJson(new InputStreamReader(body, StandardCharsets.UTF_8), classOfT);
        if (ret == null)
            throw new IOException("Failed to parse JSON body");
        return ret;
    }

    public static List<String> parseQueryParam(String query, String drivers) {
        if (query == null || query.isEmpty())
            return List.of();

        String[] params = query.split("&");
        for (String param : params) {
            String[] keyValue = param.split("=");
            if (keyValue.length == 2 && keyValue[0].equals(drivers)) {
                return List.of(keyValue[1].split(","));
            }
        }
        return List.of();
    }
}
