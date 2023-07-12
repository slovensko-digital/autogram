package digital.slovensko.autogram;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Base64;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.util.EntityUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.yaml.snakeyaml.Yaml;

import digital.slovensko.autogram.server.dto.Document;
import digital.slovensko.autogram.server.dto.ServerSigningParameters;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.dto.SignResponse;

// TODO mvn test -Psmoke
@TestInstance(Lifecycle.PER_CLASS)
@Tag("HttpSmokeTest")
public class SignHttpSmokeTest {

    Map<String, Object> data;

    URI baseUri;
    HttpClientBuilder clientBuilder;

    public static Object getNested(Object obj, String... keys) {
        for (String key : keys) {
            if (obj instanceof Map) {
                obj = ((Map<?, ?>) obj).get(key);
            } else {
                return null;
            }
        }
        return obj;
    }

    public Object getNested(String... keys) {
        return getNested(data, keys);
    }

    public String getDecoded(Object obj) {
        return new String(Base64.getDecoder().decode(((String) obj)));
    }

    public String getDecoded(String... keys) {
        return getDecoded(getNested(keys));
    }

    @BeforeAll
    public void initAll() throws FileNotFoundException, URISyntaxException {
        Yaml yaml = new Yaml();
        var inputStream = new FileInputStream(
                new File("src/main/resources/digital/slovensko/autogram/server/server.yml"));

        data = (Map<String, Object>) yaml.load(inputStream);
        baseUri = new URI("http://localhost:37200");
        clientBuilder = HttpClientBuilder.create();
    }

    @Test
    @Tag("HttpSmokeTest")
    void testSigningHappyScenario() throws URISyntaxException, ClientProtocolException, IOException {

        var signRequest = new HttpPost(baseUri.resolve("/sign"));
        signRequest.setHeader("Content-Type", "application/json");
        var signRequestBody = """
                {"document": {
                        "content": "Testovací dokument",
                        "filename": "TextDocument.txt"
                    },
                    "parameters": {
                        "level": "XAdES_BASELINE_B",
                        "container": "ASiC_E"
                    },
                    "payloadMimeType": "text/plain"
                }
                """;
        System.out.println("Sign request body: " + signRequestBody);

        signRequest.setEntity(new StringEntity(signRequestBody, "UTF-8"));
        var signResponse = clientBuilder.build().execute(signRequest);
        assertEquals(HttpStatus.SC_OK, signResponse.getStatusLine().getStatusCode());
        System.out.println("Sign Response: " + signResponse.getStatusLine());
        System.out.println("Sign Response: " + new String(
                signResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XAdES-XML-Base64-HTML_md", "XAdES-XML-Base64-TXT", "XAdES-XML-TXT-HTML",
            "XAdES-XML-TXT-TXT_md", "XAdES-ASiC_E-Base64-HTML", "XAdES-ASiC_E-Base64-TXT",
            "XAdES-ASiC_E-TXT-HTML_md", "XAdES-ASiC_E-TXT-TXT", "XAdES-ASiC_E-SKXDC-Base64-HTML",
            "XAdES-ASiC_E-SKXDC-TXT-HTML", "PAdES-PDF_lg", "XAdES-PDF",
            "XAdES-ASiC_E-PDF", "XAdES-ASiC_E-TXT", "XAdES-ASiC_E-DOCX",
            "CAdES-ASiC_E-DOCX", "CAdES-ASiC_E-PNG_md", "CAdES-PNG_lg",
    })
    public void testFromYaml(String exampleName) throws ClientProtocolException, IOException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        var example = getNested("components", "examples", exampleName, "value");
        var document = (Map<String, String>) getNested(example, "document");
        var parameters = getNested(example, "parameters");
        var payloadMimeType = getNested(example, "payloadMimeType");

        var rDocument = new Document(document.get("filename"), document.get("content"));
        var rParameters = ServerSigningParameters.fromMap((Map<String, Object>) parameters);
        var rPayloadMimeType = (String) payloadMimeType;

        var body = new SignRequestBody(
                rDocument,
                rParameters,
                rPayloadMimeType);
        var gson = new com.google.gson.Gson();

        var signRequest = new HttpPost(baseUri.resolve("/sign"));
        signRequest.setHeader("Content-Type", "application/json");
        var entity = new StringEntity(gson.toJson(body), "UTF-8");
        signRequest.setEntity(entity);

        System.out.println("Sign request body: " + EntityUtils.toString(entity));
        var signResponse = clientBuilder.build().execute(signRequest);
        System.out.println("Sign Response: " + signResponse.getStatusLine());

        var responseStr = new String(
                signResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        System.out.println("Sign Response: " + responseStr);
        assertEquals(HttpStatus.SC_OK, signResponse.getStatusLine().getStatusCode());
        var response = gson.fromJson(responseStr, SignResponse.class);

        ReflectionUtils
                .findFields(SignResponse.class, (e) -> true,
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        var o = f.get(response);
                        assertNotNull(o);
                        System.out.println(f.getName() + ": " + o);
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                });
    }

}