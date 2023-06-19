package digital.slovensko.autogram.core;

import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.UnsupportedSignatureException;
import digital.slovensko.autogram.ui.SaveFileResponder;
import eu.europa.esig.dss.asic.cades.signature.ASiCWithCAdESService;
import eu.europa.esig.dss.asic.xades.signature.ASiCWithXAdESService;
import eu.europa.esig.dss.cades.signature.CAdESService;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.DSSException;
import eu.europa.esig.dss.model.FileDocument;
import eu.europa.esig.dss.pades.signature.PAdESService;
import eu.europa.esig.dss.validation.CommonCertificateVerifier;
import eu.europa.esig.dss.xades.signature.XAdESService;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Properties;

public class DefaultSigningJob implements SigningJob {
    private final Responder responder;
    private final CommonDocument document;
    private final SigningParameters parameters;
    private final Charset encoding;

    public DefaultSigningJob(CommonDocument document, SigningParameters parameters, Responder responder) {
        this.document = document;
        this.parameters = parameters;
        this.responder = responder;
        this.encoding = StandardCharsets.UTF_8;
    }

    @Override
    public DSSDocument getDocument() {
        return this.document;
    }

    public SigningParameters getParameters() {
        return parameters;
    }

    @Override
    public boolean isPlainText() {
        var transformationMimeType = parameters.getTransformationOutputMimeType();
        if (transformationMimeType != null)
            return MimeTypeEnum.TEXT.equals(transformationMimeType);

        return document.getMimeType().equals(MimeTypeEnum.TEXT);
    }

    @Override
    public boolean isHTML() {
        return MimeTypeEnum.HTML.equals(parameters.getTransformationOutputMimeType());
    }

    @Override
    public int getVisualizationWidth() {
        return parameters.getVisualizationWidth();
    }

    @Override
    public boolean isPDF() {
        return MimeTypeEnum.PDF.equals(document.getMimeType());
    }

    @Override
    public boolean isImage() {
        return MimeTypeEnum.JPEG.equals(document.getMimeType()) || MimeTypeEnum.PNG.equals(document.getMimeType());
    }

    private boolean isXDC() {
        return AutogramMimeType.XML_DATACONTAINER.equals(document.getMimeType());
    }

    @Override
    public String getDocumentAsPlainText() throws AutogramException {
        if (document.getMimeType().equals(MimeTypeEnum.TEXT)) {
            try (var is = document.openStream()) {
                return new String(is.readAllBytes(), StandardCharsets.UTF_8);
            } catch (IOException e) {
                throw AutogramException.fromThrowable(e);
            }
        } else {
            return transform();
        }
    }

    private String transform() throws AutogramException {
        // TODO probably move this logic into signing job creation
        try (var is = this.document.openStream()) {
            var builderFactory = DocumentBuilderFactory.newInstance();
            builderFactory.setNamespaceAware(true);

            var inputSource = new InputSource(is);
            inputSource.setEncoding(encoding.displayName());
            var document = builderFactory.newDocumentBuilder().parse(inputSource);

            var xmlSource = new DOMSource(document);
            if (isXDC())
                xmlSource = extractFromXDC(document, builderFactory);

            var outputTarget = new StreamResult(new StringWriter());
            var transformer = TransformerFactory
                    .newDefaultInstance().newTransformer(
                            new StreamSource(new ByteArrayInputStream(parameters.getTransformation().getBytes(encoding)))
                    );
            var outputProperties = new Properties();
            outputProperties.setProperty(OutputKeys.ENCODING, encoding.displayName());
            transformer.setOutputProperties(outputProperties);
            transformer.transform(xmlSource, outputTarget);

            return outputTarget.getWriter().toString().trim();
        } catch (Exception e) {
            throw AutogramException.fromThrowable(e);
        }
    }

    private DOMSource extractFromXDC(Document document, DocumentBuilderFactory builderFactory) throws AutogramException {
        try {
            var xdc = document.getDocumentElement();

            var xmlData = xdc.getElementsByTagNameNS("http://data.gov.sk/def/container/xmldatacontainer+xml/1.1", "XMLData")
                    .item(0);

            if (xmlData == null)
                throw new IllegalArgumentException("XMLData not found in XDC");

            document = builderFactory.newDocumentBuilder().newDocument();
            var node = document.importNode(xmlData.getFirstChild(), true);
            document.appendChild(node);

            return new DOMSource(document);
        } catch (Exception e) {
            throw AutogramException.fromThrowable(e);
        }
    }

    @Override
    public String getDocumentAsHTML() throws AutogramException {
        return transform();
    }

    @Override
    public String getDocumentAsBase64Encoded() throws AutogramException {
        try (var is = document.openStream()) {
            return new String(Base64.getEncoder().encode(is.readAllBytes()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw AutogramException.fromThrowable(e);
        }
    }

    @Override
    public void signWithKeyAndRespond(SigningKey key) throws AutogramException {
        try {
            boolean isContainer = getParameters().getContainer() != null;
            var doc = switch (getParameters().getSignatureType()) {
                case XAdES -> isContainer ? signDocumentAsAsiCWithXAdeS(key) : signDocumentAsXAdeS(key);
                case CAdES -> isContainer ? signDocumentAsASiCWithCAdeS(key) : signDocumentAsCAdeS(key);
                case PAdES -> signDocumentAsPAdeS(key);
                default -> throw new UnsupportedSignatureException(getParameters().getSignatureType().name());
            };
            responder.onDocumentSigned(new SignedDocument(doc, key.getCertificate()));
        } catch (DSSException e) {
            throw AutogramException.fromDSSException(e);
        }
    }

    @Override
    public void onDocumentSignFailed(AutogramException e) {
        responder.onDocumentSignFailed(e);
    }

    private DSSDocument signDocumentAsCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new CAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsAsiCWithXAdeS(SigningKey key) throws AutogramException {
        DSSDocument doc = getDocument();
        if (getParameters().shouldCreateDatacontainer() && !isXDC()) {
            var transformer = DefaultXdcTransformer.buildFromSigningParameters(getParameters());
            doc = transformer.transform(getDocument());
            doc.setMimeType(AutogramMimeType.XML_DATACONTAINER);
        }

        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithXAdESService(commonCertificateVerifier);
        var signatureParameters = getParameters().getASiCWithXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(doc, signatureParameters);
        var signatureValue = key.sign(dataToSign, getParameters().getDigestAlgorithm());

        return service.signDocument(doc, signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsXAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new XAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getXAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsASiCWithCAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new ASiCWithCAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getASiCWithCAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    private DSSDocument signDocumentAsPAdeS(SigningKey key) {
        var commonCertificateVerifier = new CommonCertificateVerifier();
        var service = new PAdESService(commonCertificateVerifier);
        var jobParameters = getParameters();
        var signatureParameters = getParameters().getPAdESSignatureParameters();

        signatureParameters.setSigningCertificate(key.getCertificate());
        signatureParameters.setCertificateChain(key.getCertificateChain());

        var dataToSign = service.getDataToSign(getDocument(), signatureParameters);
        var signatureValue = key.sign(dataToSign, jobParameters.getDigestAlgorithm());

        return service.signDocument(getDocument(), signatureParameters, signatureValue);
    }

    public static DefaultSigningJob buildFromFile(File file, Autogram autogram) {
        var document = new FileDocument(file);

        SigningParameters parameters;
        var filename = file.getName();

        if (filename.endsWith(".pdf")) {
            parameters = SigningParameters.buildForPDF(filename);
        } else {
            parameters = SigningParameters.buildForASiCWithXAdES(filename);
        }

        var responder = new SaveFileResponder(file, autogram);
        return new DefaultSigningJob(document, parameters, responder);
    }

    @Override
    public boolean shouldCheckPDFCompliance() {
        return parameters.getCheckPDFACompliance();
    }
}
