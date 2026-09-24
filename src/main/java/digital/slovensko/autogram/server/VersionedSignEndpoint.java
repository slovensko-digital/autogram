package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.server.dto.VersionedSignRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

import java.io.IOException;

import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.JSON_PARSING_FAILED;

public class VersionedSignEndpoint implements HttpHandler {
    private final Autogram autogram;

    public VersionedSignEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var body = EndpointUtils.loadFromJsonExchange(exchange, VersionedSignRequestBody.class);

            var responder = new ServerResponder(exchange);
            var input = body.getSigningInput(autogram.isPlainXmlEnabled());

            if (body.batchId() != null) {
                var batch = autogram.getBatch(body.batchId());
                var job = SigningJob.fromInput(input, batch, batch.getProcessedDocumentsCount() + 1);
                autogram.batchSign(job, body.batchId(), responder);
            } else {
                var job = SigningJob.fromInput(input);
                autogram.startSigning(job, responder);
            }

        } catch (JsonSyntaxException | IOException e) {
            var response = ErrorResponseBuilder.buildFromException(new MalformedBodyException(JSON_PARSING_FAILED, e));
            EndpointUtils.respondWithError(response, exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(e), exchange);
        }
    }
}
