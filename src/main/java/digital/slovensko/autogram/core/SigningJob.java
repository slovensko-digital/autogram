package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Objects;

public class SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;

    public SigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
    }

    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    public void onDocumentSignFailed(SigningJob job, SigningError e) {
        responder.onDocumentSignFailed(job, e);
    }

    public void onDocumentSigned(SignedDocument signedDocument) {
        responder.onDocumentSigned(signedDocument);
    }

    public boolean isPlainText() {
        return Objects.equals(parameters.getTransformationOutputMimeType(), "text/plain");
    }

    public boolean isHTML() {
        return Objects.equals(parameters.getTransformationOutputMimeType(), "text/html");
    }

    public boolean isPDF() {
        return parameters.getSignatureType() == SigningParameters.SignatureType.PADES;
    }

    public String getDocumentAsPlainText() {
        return transform();
    }

    private String transform() {
        // TODO probably move this logic into signing job creation
        try {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);
            var document = builderFactory.newDocumentBuilder().parse(new InputSource(this.document.openStream()));
            document.setXmlStandalone(true);
            var xmlSource = new DOMSource(document);
            var outputTarget = new StreamResult(new StringWriter());

            var transformer = TransformerFactory.newInstance().newTransformer(
                    new StreamSource(new ByteArrayInputStream(parameters.getTransformation().getBytes()))
            );

            transformer.transform(xmlSource, outputTarget);

            var result = outputTarget.getWriter().toString().trim();
            return result;
        } catch (Exception e) {
            return null; // TODO
        }
    }

    public String getDocumentAsHTML() {
        return transform();
    }

    public String getDocumentAsBase64Encoded() {
        try {
            return new String(Base64.getEncoder().encode(document.openStream().readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
