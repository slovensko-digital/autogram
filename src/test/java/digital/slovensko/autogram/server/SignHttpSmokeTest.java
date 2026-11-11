package digital.slovensko.autogram.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.platform.commons.util.ReflectionUtils;
import org.yaml.snakeyaml.Yaml;

import digital.slovensko.autogram.server.dto.ErrorResponseBody;
import digital.slovensko.autogram.server.dto.SignResponse;


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
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XAdES-ASiC_E-Auto", "XAdES-ASiC_E-Base64-HTML", "XAdES-ASiC_E-multi-documents",
            "Signed-XAdES=ASiC_E-multi-documents", "XAdES-ASiC_E-Base64-TXT", "XAdES-ASiC_E-TXT-HTML",
            "XAdES-ASiC_E-TXT-TXT", "XAdES-ASiC_E-SKXDC-Base64-TXT", "Signed-XAdES-ASiC_E-SKXDC-Base64-HTML",
            "Signed-XAdES-ASiC_E-SKXDC-Auto", "FS-EForm-Auto", "PAdES-PDF_lg", "Signed-PAdES", "XAdES-ASiC_E-PDF",
            "CAdES-ASiC_E-PDF", "Signed-CAdES-ASiC_E-PDF", "Double-Signed-CAdES-ASiC_E-PDF", "XAdES-ASiC_E-TXT",
            "XAdES-ASiC_E-TXT-Canonicalization", "XAdES-ASiC_E_CADES_T-TXT", "XAdES-ASiC_E_XADES_T-TXT",
            "XAdES-ASiC_E-DOCX", "CAdES-ASiC_E-PNG_md",
    })
    public void testPositiveFromYaml(String exampleName) throws ClientProtocolException, IOException {
        testFromYaml(exampleName, "/api/v1/sign", HttpStatus.SC_OK, SignResponse.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XAdES-ASiC_E-SKXDC-Auto-WrongXSLT",
    })
    public void testNegative422FromYaml(String exampleName) throws ClientProtocolException, IOException {
        testFromYaml(exampleName, "/api/v1/sign", HttpStatus.SC_UNPROCESSABLE_ENTITY, ErrorResponseBody.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "legacy-XAdES-ASiC_E-Base64-HTML", "legacy-XAdES-ASiC_E-Base64-TXT", "legacy-XAdES-ASiC_E-TXT-HTML_md",
            "legacy-XAdES-ASiC_E-TXT-TXT", "legacy-XAdES-ASiC_E-Auto", "legacy-XAdES-ASiC_E-SKXDC-Base64-TXT",
            "legacy-XAdES-ASiC_E-SKXDC-TXT-TXT", "legacy-Signed-XAdES-ASiC_E-SKXDC-Base64-TXT",
            "legacy-Signed-XAdES-ASiC_E-SKXDC-Base64-HTML", "legacy-Signed-XAdES-ASiC_E-SKXDC-Auto",
            "legacy-Signed-XAdES=ASiC_E-multi-documents", "legacy-FS-EForm-Auto", "legacy-PAdES-PDF_lg",
            "legacy-Signed-PAdES", "legacy-XAdES-PDF", "legacy-XAdES-ASiC_E-PDF", "legacy-CAdES-ASiC_E-PDF",
            "legacy-XAdES-ASiC_E-TXT", "legacy-XAdES-ASiC_E-TXT-Canonicalization", "legacy-XAdES-ASiC_E_CADES_T-TXT",
            "legacy-XAdES-ASiC_E_XADES_T-TXT", "legacy-XAdES-ASiC_E-DOCX", "legacy-CAdES-ASiC_E-DOCX",
            "legacy-CAdES-PNG_lg", "legacy-CAdES-ASiC_E-PNG_md", "legacy-Signed-XAdES-ASiC_E-PDF",
            "legacy-Double-Signed-XAdES-ASiC_E-PDF", "legacy-Signed-CAdES-ASiC_E-PDF",
            "legacy-Double-Signed-CAdES-ASiC_E-PDF",
    })
    public void testLegacyPositiveFromYaml(String exampleName) throws ClientProtocolException, IOException {
        testFromYaml(exampleName, "/sign", HttpStatus.SC_OK, SignResponse.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "legacy-XAdES-ASiC_E-SKXDC-Auto-WrongXSLT",
    })
    public void testLegacyNegative422FromYaml(String exampleName) throws ClientProtocolException, IOException {
        testFromYaml(exampleName, "/sign", HttpStatus.SC_UNPROCESSABLE_ENTITY, ErrorResponseBody.class);
    }

    public void testFromYaml(String exampleName, String path, int expectedStatus, Class responseClass)
            throws ClientProtocolException, IOException {
        System.out.println("Testing example: " + exampleName);
        var example = getNested("components", "examples", exampleName, "value");
        assertNotNull(example, "Example not found in server.yml: " + exampleName);

        // the examples document the request body of the path, so they are sent as they are
        var gson = new com.google.gson.Gson();
        var signRequest = new HttpPost(baseUri.resolve(path));
        signRequest.setHeader("Content-Type", "application/json");
        signRequest.setEntity(new StringEntity(gson.toJson(example), "UTF-8"));

        var signResponse = clientBuilder.build().execute(signRequest);
        System.out.println("Sign Response: " + signResponse.getStatusLine());

        var responseStr = new String(
                signResponse.getEntity().getContent().readAllBytes(), StandardCharsets.UTF_8);
        assertEquals(expectedStatus, signResponse.getStatusLine().getStatusCode());
        var response = gson.fromJson(responseStr, responseClass);

        ReflectionUtils
                .findFields(responseClass, (e) -> true,
                        ReflectionUtils.HierarchyTraversalMode.TOP_DOWN)
                .forEach(f -> {
                    f.setAccessible(true);
                    try {
                        var o = f.get(response);
                        assertNotNull(o);
                    } catch (IllegalArgumentException | IllegalAccessException e1) {
                        e1.printStackTrace();
                    }
                });
    }
}
