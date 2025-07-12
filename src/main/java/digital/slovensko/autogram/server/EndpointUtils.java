package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;

import digital.slovensko.autogram.core.errors.ResponseNetworkErrorException;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.EmptyBodyException;
import java.io.IOException;
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
            throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
        }
    }

    public static void respondWith(Object response, HttpExchange exchange) {
        try {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
            exchange.getResponseBody().close();
        } catch (IOException e) {
            throw new ResponseNetworkErrorException("Externá aplikácia nečakala na odpoveď", e);
        }
    }

    public static <T> T loadFromJsonExchange(HttpExchange exchange, Class<T> classOfT) throws IOException {
        var content = new String(exchange.getRequestBody().readAllBytes());
        if (content == null || content.isEmpty())
            throw new EmptyBodyException("Empty body");
        var ret = gson.fromJson(content, classOfT);
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
