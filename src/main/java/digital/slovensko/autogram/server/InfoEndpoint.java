package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class InfoEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO wait for ready state and return if ready
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, 0);
        
        var msg = "{\"version\": \"1.2.3\",\"status\": \"READY\"}";
        exchange.getResponseBody().write(msg.getBytes());
        exchange.getResponseBody().close();
    }
}
