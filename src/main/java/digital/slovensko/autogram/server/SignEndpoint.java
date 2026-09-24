package digital.slovensko.autogram.server;

import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.SigningJob;

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
            var body = EndpointUtils.loadFromJsonExchange(exchange, SignRequestBody.class);
            body.validateDocument();
            body.validateSigningParameters();

            var responder = new ServerResponder(exchange);
            var input = body.getSigningInput(autogram.isPlainXmlEnabled());

            if (body.getBatchId() != null) {
                var batch = autogram.getBatch(body.getBatchId());
                var job = SigningJob.fromInput(input, batch);
                autogram.submitToBatch(job, body.getBatchId(), responder);
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
