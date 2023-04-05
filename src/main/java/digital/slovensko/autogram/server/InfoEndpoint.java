package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;

public class InfoEndpoint implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        // TODO wait for ready state and return if ready
    }
}
