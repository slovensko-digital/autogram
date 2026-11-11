package digital.slovensko.autogram.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpHeaders;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

import digital.slovensko.autogram.server.dto.BatchEndResponseBody;
import digital.slovensko.autogram.server.dto.BatchStartResponseBody;

@Tag("HttpSmokeTest")
public class VersionedApiHttpSmokeTests {
    private final URI baseUri;
    private final HttpClientBuilder clientBuilder;

    public VersionedApiHttpSmokeTests() throws URISyntaxException {
        this.baseUri = new URI("http://localhost:37200");
        this.clientBuilder = HttpClientBuilder.create();
    }

    @Test
    void testVersionedInfoEndpointReturnsJson() throws ClientProtocolException, IOException {
        var request = new HttpGet(baseUri.resolve("/api/v1/info"));

        var response = clientBuilder.build().execute(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue().startsWith("application/json"));
    }

    @Test
    void testVersionedDocsEndpointReturnsHtml() throws ClientProtocolException, IOException {
        var request = new HttpGet(baseUri.resolve("/api/v1/docs"));

        var response = clientBuilder.build().execute(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue().startsWith("text/html"));
    }

    @Test
    void testVersionedAssetsEndpointReturnsAsset() throws ClientProtocolException, IOException {
        var request = new HttpGet(baseUri.resolve("/api/v1/assets/swagger-ui-v5.11.0.css"));

        var response = clientBuilder.build().execute(request);

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        assertTrue(response.getFirstHeader(HttpHeaders.CONTENT_TYPE).getValue().startsWith("text/css"));
    }

    @Test
    void testVersionedBatchEndpointStartsBatch() throws ClientProtocolException, IOException {
        var request = new HttpPost(baseUri.resolve("/api/v1/batch"));
        request.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        request.setEntity(new StringEntity("{\"totalNumberOfDocuments\": 1}", "UTF-8"));

        var response = clientBuilder.build().execute(request);
        var json = EntityUtils.toString(response.getEntity());

        assertEquals(HttpStatus.SC_OK, response.getStatusLine().getStatusCode());
        var batchId = new Gson().fromJson(json, BatchStartResponseBody.class).batchId();
        assertNotNull(batchId);

        // end the empty batch right away, documents without batchId are rejected while it runs
        var endRequest = new HttpEntityEnclosingRequestBase() {
            @Override
            public String getMethod() {
                return "DELETE";
            }
        };
        endRequest.setURI(baseUri.resolve("/api/v1/batch"));
        endRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");
        endRequest.setEntity(new StringEntity("{\"batchId\": \"%s\"}".formatted(batchId), "UTF-8"));

        var endResponse = clientBuilder.build().execute(endRequest);
        var endJson = EntityUtils.toString(endResponse.getEntity());

        assertEquals(HttpStatus.SC_OK, endResponse.getStatusLine().getStatusCode());
        assertNotNull(new Gson().fromJson(endJson, BatchEndResponseBody.class).status());
    }
}