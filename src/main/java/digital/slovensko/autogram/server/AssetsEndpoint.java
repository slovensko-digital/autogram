package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.InvalidUrlParamException;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class AssetsEndpoint implements HttpHandler {
    private static final List<File> assets = new ArrayList<>();

    static  {
        var location = AssetsEndpoint.class.getProtectionDomain().getCodeSource().getLocation().getPath();
        var path = Path.of(location,"digital", "slovensko", "autogram", "server", "assets");

        Collections.addAll(assets, requireNonNull(path.toFile().listFiles()));
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var uriPath = exchange.getRequestURI().getPath().substring(7); // remove /assets

        try {
            if (uriPath.isBlank()) {
                throw new InvalidUrlParamException("Missing asset name");
            }

            uriPath = uriPath.substring(1); // remove slash

            final String uriPathFinal = uriPath;
            final File file = assets.stream()
                    .filter(f -> f.getName().equals(uriPathFinal))
                    .findFirst()
                    .orElse(null);

            if (file == null) {
                throw new InvalidUrlParamException("Asset with this name does not exist");
            }

            var stream = getClass().getResourceAsStream("assets/" + file.getName());
            if (stream == null) {
                throw new InvalidUrlParamException("Asset with this name does not exist");
            }

            try (exchange) {
                exchange.getResponseHeaders().set("Content-Type", Files.probeContentType(file.toPath()));
                exchange.sendResponseHeaders(200, 0);
                requireNonNull(stream).transferTo(exchange.getResponseBody());
            }

            stream.close();
        } catch (IOException | InvalidUrlParamException e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
