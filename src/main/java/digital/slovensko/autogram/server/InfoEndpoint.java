package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class InfoEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO wait for ready state and return if ready
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Authorization");

        // Allow preflight requests
        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            exchange.getResponseBody().close();
            return;
        }

        exchange.sendResponseHeaders(200, 0);
        var msg = "{\"version\": \"1.2.3\",\"status\": \"READY\"}";
        try {
            exchange.getResponseBody().write(msg.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        exchange.getResponseBody().close();
    }
}
