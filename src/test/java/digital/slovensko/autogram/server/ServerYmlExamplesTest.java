package digital.slovensko.autogram.server;

import com.google.gson.Gson;
import digital.slovensko.autogram.TestAutogramFactory;
import digital.slovensko.autogram.core.Responder;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.dto.SignedDocument;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.visualization.UnsupportedVisualization;
import digital.slovensko.autogram.server.dto.SignRequestBody;
import digital.slovensko.autogram.server.dto.VersionedSignRequestBody;
import eu.europa.esig.dss.enumerations.SignatureProfile;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

public class ServerYmlExamplesTest {
    private static final String YML_PATH = "digital/slovensko/autogram/server/server.yml";

    private static final Set<String> EXPECTED_UNSUPPORTED_EXTENSIONS = Set.of(".docx");

    private static Map<String, Object> loadYml() {
        try (InputStream in = ServerYmlExamplesTest.class.getClassLoader().getResourceAsStream(YML_PATH)) {
            return new Yaml().<Map<String, Object>>load(in);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> componentsExamples(Map<String, Object> root) {
        var components = (Map<String, Object>) root.get("components");
        return (Map<String, Object>) components.get("examples");
    }

    @SuppressWarnings("unchecked")
    private static List<String> positiveExampleNames(Map<String, Object> root, String path) {
        var paths = (Map<String, Object>) root.get("paths");
        var post = (Map<String, Object>) ((Map<String, Object>) paths.get(path)).get("post");
        var requestBody = (Map<String, Object>) post.get("requestBody");
        var content = (Map<String, Object>) requestBody.get("content");
        var json = (Map<String, Object>) content.get("application/json");
        var examples = (Map<String, Object>) json.get("examples");

        var names = new ArrayList<String>();
        for (var entry : examples.entrySet()) {
            var refHolder = (Map<String, Object>) entry.getValue();
            var ref = (String) refHolder.get("$ref");
            var name = ref.substring(ref.lastIndexOf('/') + 1);
            if (name.contains("WrongXSLT"))
                continue;
            names.add(name);
        }

        return names;
    }

    @SuppressWarnings("unchecked")
    private static Object exampleValue(Map<String, Object> examplesComponents, String name) {
        var example = (Map<String, Object>) examplesComponents.get(name);
        if (example == null)
            fail("No such example in components.examples: " + name);

        return example.get("value");
    }

    private static void assertVisualizationsAreSupported(String exampleName, SigningJob job) {
        for (var visualization : job.getVisualizations()) {
            var documentName = visualization.getName();
            var isExpectedUnsupported = EXPECTED_UNSUPPORTED_EXTENSIONS.stream()
                    .anyMatch(extension -> documentName.toLowerCase().endsWith(extension));

            assertTrue(isExpectedUnsupported || !(visualization instanceof UnsupportedVisualization),
                    exampleName + ": document '" + documentName + "' has no supported visualization");
        }
    }

    private static void signAndAssertSuccess(String exampleName, SigningJob job, RecordingResponder responder) {
        var autogram = TestAutogramFactory.create();
        autogram.pickSigningKeyAndThen(key -> autogram.sign(job, key));

        assertTrue(responder.signed, exampleName + ": signing did not report success");
    }

    private static class RecordingResponder extends Responder {
        private boolean signed = false;

        @Override
        public void onDocumentSigned(SignedDocument signedDocument) {
            signed = true;
        }

        @Override
        public void onDocumentSignFailed(AutogramException error) {
        }
    }

    @TestFactory
    List<DynamicTest> versionedSignExamplesBuildVisualizeAndSignSuccessfully() {
        var root = loadYml();
        var examplesComponents = componentsExamples(root);
        var gson = new Gson();
        var tests = new ArrayList<DynamicTest>();

        for (var name : positiveExampleNames(root, "/api/v1/sign")) {
            tests.add(DynamicTest.dynamicTest(name, () -> {
                var value = exampleValue(examplesComponents, name);
                var json = gson.toJson(value);
                var body = gson.fromJson(json, VersionedSignRequestBody.class);

                var input = body.getSigningInput(true);
                var responder = new RecordingResponder();
                var job = SigningJob.fromInput(input, responder);
                job.initializeVisualizations();

                assertVisualizationsAreSupported(name, job);

                // TODO mock TSP in order to test BASELINE_T examples reliably
                if (job.getParameters().getSignatureProfile() == SignatureProfile.BASELINE_B)
                    signAndAssertSuccess(name, job, responder);
            }));
        }

        return tests;
    }

    @TestFactory
    List<DynamicTest> legacySignExamplesBuildVisualizeAndSignSuccessfully() {
        var root = loadYml();
        var examplesComponents = componentsExamples(root);
        var gson = new Gson();
        var tests = new ArrayList<DynamicTest>();

        for (var name : positiveExampleNames(root, "/sign")) {
            tests.add(DynamicTest.dynamicTest(name, () -> {
                var value = exampleValue(examplesComponents, name);
                var json = gson.toJson(value);
                var body = gson.fromJson(json, SignRequestBody.class);

                body.validateDocument();
                body.validateSigningParameters();
                var input = body.getSigningInput(true);
                var responder = new RecordingResponder();
                var job = SigningJob.fromInput(input, responder);
                job.initializeVisualizations();

                assertVisualizationsAreSupported(name, job);

                // TODO mock TSP in order to test BASELINE_T examples reliably
                if (job.getParameters().getSignatureProfile() == SignatureProfile.BASELINE_B)
                    signAndAssertSuccess(name, job, responder);
            }));
        }

        return tests;
    }
}
