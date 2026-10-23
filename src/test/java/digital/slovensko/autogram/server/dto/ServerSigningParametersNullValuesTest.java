package digital.slovensko.autogram.server.dto;

import static org.junit.jupiter.api.Assertions.assertFalse;

import org.junit.jupiter.api.Test;

import com.google.gson.Gson;

class ServerSigningParametersNullValuesTest {
    private final Gson gson = new Gson();

    @Test
    void acceptsNullForOptionalBooleanParameters() {
        // Clients such as Autogram Portal send every optional parameter, the ones they do not set as null.
        var body = gson.fromJson("""
                {
                  "document": { "filename": "dpfo.xml", "content": "PGEvPg==" },
                  "parameters": {
                    "level": "XAdES_BASELINE_B",
                    "container": "ASiC_E",
                    "autoLoadEform": true,
                    "containerXmlns": null,
                    "embedUsedSchemas": null,
                    "checkPDFACompliance": null,
                    "identifier": null,
                    "fsFormId": "833_825"
                  },
                  "payloadMimeType": "application/xml;base64"
                }
                """, SignRequestBody.class);

        assertFalse(body.parameters().toXDCParameters().getEmbedUsedSchemas());
        assertFalse(body.parameters().toVersionedParameters().toSigningParameters(null, false).getCheckPDFACompliance());
    }
}
