package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import digital.slovensko.autogram.server.dto.ErrorResponse;

import java.io.IOException;

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
            throw new RuntimeException(e);
        }
    }

    public static void respondWith(Object response, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T loadFromJsonExchange(HttpExchange exchange, Class<T> classOfT) throws IOException {
        return gson.fromJson(new String(exchange.getRequestBody().readAllBytes()), classOfT);
    }
}
