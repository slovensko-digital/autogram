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

import digital.slovensko.autogram.server.dto.Document;
import digital.slovensko.autogram.server.dto.ErrorResponseBody;
import digital.slovensko.autogram.server.dto.ServerSigningParameters;
import digital.slovensko.autogram.server.dto.ServerSigningParameters.LocalCanonicalizationMethod;
import digital.slovensko.autogram.server.dto.ServerSigningParameters.VisualizationWidthEnum;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.DigestAlgorithm;
import eu.europa.esig.dss.enumerations.SignatureLevel;
import eu.europa.esig.dss.enumerations.SignaturePackaging;
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
        // System.out.println("Sign request body: " + signRequestBody);

        signRequest.setEntity(new StringEntity(signRequestBody, "UTF-8"));
        var signResponse = clientBuilder.build().execute(signRequest);
        assertEquals(HttpStatus.SC_OK, signResponse.getStatusLine().getStatusCode());
        // System.out.println("Sign Response: " + signResponse.getStatusLine());
        // System.out.println("Sign Response: " + new String(
        // signResponse.getEntity().getContent().readAllBytes(),
        // StandardCharsets.UTF_8));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "XAdES-XML-Base64-HTML_md", "XAdES-XML-Base64-TXT", "XAdES-XML-TXT-HTML", "XAdES-XML-TXT-TXT_md",
            "XAdES-ASiC_E-Base64-HTML", "XAdES-ASiC_E-Base64-TXT", "XAdES-ASiC_E-TXT-HTML_md", "XAdES-ASiC_E-TXT-TXT",
            "XAdES-ASiC_E-Auto", "XAdES-ASiC_E-SKXDC-Base64-TXT", "XAdES-ASiC_E-SKXDC-TXT-TXT",
            "Signed-XAdES-ASiC_E-SKXDC-Base64-TXT", "Signed-XAdES-ASiC_E-SKXDC-Base64-HTML",
            "Signed-XAdES-ASiC_E-SKXDC-Auto", "PAdES-PDF_lg", "XAdES-PDF", "XAdES-ASiC_E-PDF", "CAdES-ASiC_E-PDF",
            "XAdES-ASiC_E-TXT", "XAdES-ASiC_E-DOCX", "CAdES-ASiC_E-DOCX", "CAdES-PNG_lg", "CAdES-ASiC_E-PNG_md",
            "Signed-XAdES-ASiC_E-PDF", "Double-Signed-XAdES-ASiC_E-PDF", "Signed-CAdES-ASiC_E-PDF",
            "Double-Signed-CAdES-ASiC_E-PDF",
    })
    public void testPositiveFromYaml(String exampleName)
            throws ClientProtocolException, IOException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        testFromYaml(exampleName, HttpStatus.SC_OK, SignResponse.class);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "Signed-XAdES-ASiC_E-SKXDC-Auto-WrongXSLT",
    })
    public void testNegativeFromYaml(String exampleName)
            throws ClientProtocolException, IOException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        testFromYaml(exampleName, HttpStatus.SC_UNPROCESSABLE_ENTITY, ErrorResponseBody.class);
    }

    public void testFromYaml(String exampleName, int expectedStatus, Class responseClass)
            throws ClientProtocolException, IOException, IllegalAccessException,
            NoSuchFieldException, SecurityException {
        System.out.println("Testing example: " + exampleName);
        var example = getNested("components", "examples", exampleName, "value");
        var document = (Map<String, String>) getNested(example, "document");
        var parameters = getNested(example, "parameters");
        var payloadMimeType = getNested(example, "payloadMimeType");

        var rDocument = new Document(document.get("filename"), document.get("content"));
        var rParameters = fromMap((Map<String, Object>) parameters);
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
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                });
    }

    private static ServerSigningParameters fromMap(Map<String, Object> map) {
        var level = SignatureLevel.valueByName((String) map.get("level"));
        var container = fromMapToEnum(ASiCContainerType.class, map.get("container"));
        var containerFilename = (String) map.get("containerFilename");
        var containerXmlns = (String) map.get("containerXmlns");
        var packaging = fromMapToEnum(SignaturePackaging.class, map.get("packaging"));
        var digestAlgorithm = fromMapToEnum(DigestAlgorithm.class, map.get("digestAlgorithm"));
        var en319132 = (boolean) map.getOrDefault("en319132", false);
        var infoCanonicalization = fromMapToEnum(LocalCanonicalizationMethod.class, map.get("infoCanonicalization"));
        var propertiesCanonicalization = fromMapToEnum(LocalCanonicalizationMethod.class,
                map.get("propertiesCanonicalization"));
        var keyInfoCanonicalization = fromMapToEnum(LocalCanonicalizationMethod.class,
                map.get("keyInfoCanonicalization"));
        var schema = (String) map.get("schema");
        var transformation = (String) map.get("transformation");
        var identifier = (String) map.get("identifier");
        var checkPDFACompliance = (boolean) map.getOrDefault("checkPDFACompliance", false);
        var visualizationWidth = fromMapToEnum(VisualizationWidthEnum.class, map.get("visualizationWidth"));
        var autoLoadEform = (boolean) map.getOrDefault("autoLoadEform", false);

        return new ServerSigningParameters(
                level,
                container,
                containerFilename,
                containerXmlns,
                packaging,
                digestAlgorithm,
                en319132,
                infoCanonicalization,
                propertiesCanonicalization,
                keyInfoCanonicalization,
                schema,
                transformation,
                identifier,
                checkPDFACompliance,
                visualizationWidth,
                autoLoadEform);
    }

    private static <T extends Enum<T>> T fromMapToEnum(Class<T> clazz, Object obj) {
        var visualizationWidthStr = (String) obj;
        if (visualizationWidthStr == null)
            return null;
        return T.valueOf(clazz, visualizationWidthStr);
    }
}
