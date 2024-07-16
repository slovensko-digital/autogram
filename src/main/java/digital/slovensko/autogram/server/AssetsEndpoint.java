package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.InvalidUrlParamException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class AssetsEndpoint implements HttpHandler {
    private static final List<String> assets;
    private static final Path assetsPath;
    static  {
        assetsPath = Path.of(requireNonNull(AssetsEndpoint.class.getResource("index.html")).getPath()).getParent().resolve("assets");
        assets = List.of(
                "swagger-ui-bundle-v5.11.0.js",
                "swagger-ui-v5.11.0.css"
        );
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
                exchange.getResponseHeaders().set("Content-Type", Files.probeContentType(assetsPath.resolve(fileName)));
                exchange.sendResponseHeaders(200, 0);
                requireNonNull(stream).transferTo(exchange.getResponseBody());
            }

            stream.close();
        } catch (IOException | InvalidUrlParamException e) {
            EndpointUtils.respondWithError(ErrorResponse.buildFromException(e), exchange);
        }
    }
}
