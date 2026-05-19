package digital.slovensko.autogram.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.server.dto.ErrorResponse;
import digital.slovensko.autogram.server.errors.InvalidUrlParamException;

import java.io.IOException;
import java.util.List;

import static digital.slovensko.autogram.server.errors.InvalidUrlParamException.Error.ASSET_NOT_FOUND;
import static digital.slovensko.autogram.server.errors.InvalidUrlParamException.Error.MISSING_ASSET_NAME;
import static java.util.Objects.requireNonNull;

public class AssetsEndpoint implements HttpHandler {
    private static final List<String> assets;
    static  {
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
                throw new InvalidUrlParamException(MISSING_ASSET_NAME);

            if (!assets.contains(fileName))
                throw new InvalidUrlParamException(ASSET_NOT_FOUND);

            var stream = getClass().getResourceAsStream("assets/" + fileName);
            if (stream == null)
                throw new InvalidUrlParamException(ASSET_NOT_FOUND);

            try (exchange) {
                exchange.getResponseHeaders().set("Content-Type", getAssetMimeType(fileName));
                exchange.sendResponseHeaders(200, 0);
                requireNonNull(stream).transferTo(exchange.getResponseBody());
            }

            stream.close();
        } catch (IOException | InvalidUrlParamException e) {
            EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(e), exchange);
        }
    }

    private String getAssetMimeType(String fileName) {
        if (fileName.endsWith(".js"))
            return "application/javascript";

        if (fileName.endsWith(".css"))
            return "text/css";

        return "applicaiton/octet-stream";
    }
}
