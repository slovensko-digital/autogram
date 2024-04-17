package digital.slovensko.autogram.core.eforms;

import com.google.gson.Gson;
import digital.slovensko.autogram.core.eforms.dto.EFormAttributes;
import digital.slovensko.autogram.core.errors.XMLValidationException;

public class FsEFormResources extends EFormResources {
    private static final String SOURCE_URL = "https://autogram.slovensko.digital/public/eforms/fs/";
    private static final Gson gson = new Gson();
    private EFormAttributes eFormAttributes;

    private final String fsFormId;

    public FsEFormResources(String fsFormId, String canonicalizationMethod, String xsdDigest, String xsltDigest) {
        super(null, xsdDigest, xsltDigest, canonicalizationMethod);
        this.fsFormId = fsFormId;
        this.embedUsedSchemas = false;
    }

    @Override
    public boolean findResources() throws XMLValidationException {
        var doc = EFormUtils.getResource(SOURCE_URL + fsFormId + "/sign-data.json");
        if (doc == null)
            return false;

        eFormAttributes = gson.fromJson(new String(doc), EFormAttributes.class);

        return eFormAttributes != null;
    }

    @Override
    public EFormAttributes getEformAttributes() {
        return eFormAttributes;
    }
}