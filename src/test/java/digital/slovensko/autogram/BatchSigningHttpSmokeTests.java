package digital.slovensko.autogram;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.dto.BatchSessionStartResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class BatchSigningHttpSmokeTests {
    @Test
    void testBatchSigningHappyScenario() throws URISyntaxException {
        System.out.println("Session Start start!!");
        URI baseUri = new URI("http://localhost:37200");
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        HttpPost startRequest = new HttpPost(baseUri.resolve("/batch"));
        startRequest.setHeader("Content-Type", "application/json");
        startRequest.setEntity(new StringEntity("{\"totalNumberOfDocuments\": 3}", "UTF-8"));
        try {
            var startResponse = clientBuilder.build().execute(startRequest);

            // print response
            System.out.println("Session Start Response: " + startResponse.getStatusLine());
            System.out.println(
                    "Session Start Response: " + EntityUtils.toString(startResponse.getEntity()));

            assertEquals(HttpStatus.SC_OK, startResponse.getStatusLine().getStatusCode());

            var startData = new Gson().fromJson(EntityUtils.toString(startResponse.getEntity()),
                    BatchSessionStartResponseBody.class);
            System.out.println("Session Start Response: " + startData);

            var batchId = startData.batchId();
            System.out.println("Session Start BatchId: " + batchId);

            var signRequest = new HttpPost(baseUri.resolve("/sign"));
            signRequest.setHeader("Content-Type", "application/json");
            var signRequestBody = """
                {"batchId":"%s",
                    "document": {
                        "content": "Testovací dokument",
                        "filename": "TextDocument.txt"
                    },
                    "parameters": {
                        "level": "XAdES_BASELINE_B",
                        "container": "ASiC_E"
                    },
                    "payloadMimeType": "text/plain"
                }
                """.formatted(batchId);
            System.out.println("Sign request body: " + signRequestBody);
                
            signRequest.setEntity(new StringEntity(signRequestBody, "UTF-8"));
            var signResponse = clientBuilder.build().execute(signRequest);
            assertEquals(HttpStatus.SC_OK, signResponse.getStatusLine().getStatusCode());
            System.out.println("Sign Response: " + signResponse.getStatusLine());
            System.out.println("Sign Response: " + new String(
                    signResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));

        } catch (IOException e) {
            // TODO Auto-generated catch block
        }

    }
}
