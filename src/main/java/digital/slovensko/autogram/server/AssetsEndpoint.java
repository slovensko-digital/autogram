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
    private static final List<String> assets = new ArrayList<>();
    private static final Path path;

    static  {
        var parentPath = Path.of(requireNonNull(AssetsEndpoint.class.getResource("index.html")).getPath()).getParent().toString();
        path = Path.of(parentPath, "assets");

        for (var file : requireNonNull(path.toFile().listFiles()))
            assets.add(file.getName());
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        var fileName = exchange.getRequestURI().getPath().replaceFirst("/assets/?", "");

        try {
            if (fileName.isBlank())
                throw new InvalidUrlParamException("Missing asset name");

            if (!assets.contains(fileName))
                throw new InvalidUrlParamException("Asset with this name does not exist");

            var stream = getClass().getResourceAsStream("assets/" + fileName);
            if (stream == null)
                throw new InvalidUrlParamException("Asset with this name does not exist");

            try (exchange) {
                exchange.getResponseHeaders().set("Content-Type", Files.probeContentType(path.resolve(fileName)));
                exchange.sendResponseHeaders(200, 0);
                requireNonNull(stream).transferTo(exchange.getResponseBody());
            }

            stream.close();
        } catch (IOException | InvalidUrlParamException e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}