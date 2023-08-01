package digital.slovensko.autogram;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.dto.BatchStartResponseBody;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Tag("HttpSmokeTest")
public class BatchSigningHttpSmokeTests {
    @Test
    void testBatchSigningHappyScenario() throws URISyntaxException, ClientProtocolException, IOException {
        System.out.println("Session Start start!!");
        URI baseUri = new URI("http://localhost:37200");
        HttpClientBuilder clientBuilder = HttpClientBuilder.create();

        HttpPost startRequest = new HttpPost(baseUri.resolve("/batch"));
        startRequest.setHeader("Content-Type", "application/json");
        startRequest.setEntity(new StringEntity("{\"totalNumberOfDocuments\": 3}", "UTF-8"));

        var startResponse = clientBuilder.build().execute(startRequest);

        // print response
        System.out.println("Session Start Response: " + startResponse.getStatusLine());
        var json = EntityUtils.toString(startResponse.getEntity());
        System.out.println(
                "Session Start Response: " + json);

        assertEquals(HttpStatus.SC_OK, startResponse.getStatusLine().getStatusCode());

        var startData = new Gson().fromJson(json, BatchStartResponseBody.class);
        System.out.println("Session Start Response: " + startData);

        var batchId = startData.batchId();
        System.out.println("Session Start BatchId: " + batchId);

        for (var i = 0; i < 3; i++) {
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
        }

    }
}
