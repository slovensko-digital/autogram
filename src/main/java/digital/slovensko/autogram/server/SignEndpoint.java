package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.ResponderInBatch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.errors.MalformedBodyException;

import java.io.IOException;

import static digital.slovensko.autogram.server.errors.MalformedBodyException.Error.JSON_PARSING_FAILED;

public class SignEndpoint implements HttpHandler {
    private final Autogram autogram;

    public SignEndpoint(Autogram autogram) {
        this.autogram = autogram;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            var legacyBody = EndpointUtils.loadFromJsonExchange(exchange, SignRequestBody.class);
            var body = legacyBody.toVersionedBody();

            var responder = body.batchId() == null ? new ServerResponder(exchange)
                    : new ResponderInBatch(new ServerResponder(exchange), autogram.getBatch(body.batchId()));
                var job = SigningJob.fromInput(
                    body.getSigningInput(autogram.isPlainXmlEnabled()), responder);

            if (body.batchId() != null)
                autogram.batchSign(job, body.batchId());
            else
                autogram.sign(job);

        } catch (JsonSyntaxException | IOException e) {
            var response = ErrorResponseBuilder.buildFromException(new MalformedBodyException(JSON_PARSING_FAILED, e));
            EndpointUtils.respondWithError(response, exchange);

        } catch (AutogramException e) {
            EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(e), exchange);

        } catch (Exception e) {
            EndpointUtils.respondWithError(ErrorResponseBuilder.buildFromException(e), exchange);
        }
    }
}
