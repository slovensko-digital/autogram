package com.octosign.whitelabel.communication.server.endpoint;

import com.octosign.whitelabel.communication.*;
import com.octosign.whitelabel.communication.document.Document;
import com.octosign.whitelabel.communication.document.PDFDocument;
import com.octosign.whitelabel.communication.document.XMLDocument;
import com.octosign.whitelabel.communication.server.Request;
import com.octosign.whitelabel.communication.server.Response;
import com.octosign.whitelabel.communication.server.Server;
import com.octosign.whitelabel.error_handling.Code;
import com.octosign.whitelabel.error_handling.IntegrationException;
import com.octosign.whitelabel.error_handling.UserException;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.concurrent.Future;
import java.util.function.Function;

import static com.google.common.base.CaseFormat.LOWER_HYPHEN;
import static com.google.common.base.CaseFormat.UPPER_UNDERSCORE;
import static com.octosign.whitelabel.ui.I18n.translate;
import static com.octosign.whitelabel.ui.Utils.isNullOrBlank;

public class SignEndpoint extends WriteEndpoint<SignRequest, Document> {

    private Function<SignatureUnit, Future<Document>> onSign;

    public SignEndpoint(Server server, int initialNonce) {
        super(server, initialNonce);
    }

    public void setOnSign(Function<SignatureUnit, Future<Document>> onSign) {
        this.onSign = onSign;
    }

    @Override
    protected Response<Document> handleRequest(Request<SignRequest> request, Response<Document> response) {
        if (onSign == null) throw new UserException(Code.NOT_READY, translate("error.serverNotReady"));

        var signRequest = request.getBody();
        var document = Document.getSpecificDocument(signRequest);
//        var template = extractTemplateFrom(request);
        var parameters = resolveParameters(signRequest, null);
        var signatureUnit = new SignatureUnit(document, parameters);

        try {
            var signedDocument = onSign.apply(signatureUnit).get();

            return response.setBody(signedDocument);
        } catch (Exception e) {
            // TODO: We should do a better job with the error response here:
            // We can differentiate between application errors (500), user errors (502), missing certificate/UI closed (503)
            return null;
        }
    }

    @Override
    protected Class<SignRequest> getRequestClass() {
        return SignRequest.class;
    }

    @Override
    protected Class<Document> getResponseClass() {
        return Document.class;
    }

    @Override
    protected String[] getAllowedMethods() { return new String[] { "POST" }; }

    private static Configuration extractTemplateFrom(Request<?> request) {
        var templateId = request.getQueryParams().get("template");
        if (isNullOrBlank(templateId))
            return null;

        var templateName = LOWER_HYPHEN.to(UPPER_UNDERSCORE, templateId);
        return Configuration.from(templateName);
    }

    private static SignatureParameters resolveParameters(SignRequest signRequest, Configuration template) {

        return signRequest.getParameters();

//        var sourceParams = isNullOrBlank(template) ? template.parameters() : signRequest.getParameters();
//
//        return new SignatureParameters.Builder(sourceParams)
//                .schema(sourceParams.getSchema())
//                .transformation(sourceParams.getTransformation())
//                .signaturePolicyId(sourceParams.getSignaturePolicyId())
//                .signaturePolicyContent(sourceParams.getSignaturePolicyContent())
//                .build();
    }
}
