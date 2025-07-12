package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.server.dto.InfoResponse;

import static digital.slovensko.autogram.server.dto.InfoResponse.*;

import java.io.IOException;

public class InfoEndpoint implements HttpHandler {
    private final Autogram autogram;

    public InfoEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var response = new InfoResponse(
                Main.getVersionString(),
                getStatus(),
                autogram.getAvailableDrivers().stream()
                        .map(TokenDriver::getShortname)
                        .toList()
        );
        var gson = new Gson();

        try (exchange) {
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, 0);
            exchange.getResponseBody().write(gson.toJson(response).getBytes());
        }
    }
}
