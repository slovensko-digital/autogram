package digital.slovensko.autogram.server.dto;

import digital.slovensko.autogram.server.errors.RequestValidationException;
import eu.europa.esig.dss.enumerations.SignerTextPosition;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;
import eu.europa.esig.dss.pades.SignatureImageTextParameters;

import static digital.slovensko.autogram.server.errors.RequestValidationException.Error.MISSING_FIELD;

public class VisibleSignature {
    private String fieldId;
    private VisibleSignatureImage image;
    private String text;

    public String getFieldId() {
        return fieldId;
    }

    public VisibleSignatureImage getImage() {
        return image;
    }

    public String getText() {
        return text;
    }

    public void validate(String labelPrefix) {
        if (fieldId == null || fieldId.isBlank())
            throw new RequestValidationException(MISSING_FIELD, labelPrefix + ".FieldId");

        if (image == null && (text == null || text.isBlank()))
            throw new RequestValidationException(MISSING_FIELD, labelPrefix + ".Image or " + labelPrefix + ".Text");

        if (image != null)
            image.validate(labelPrefix + ".Image");
    }

    public SignatureImageParameters toDssParameters() {
        var imageParameters = new SignatureImageParameters();
        var fieldParameters = new SignatureFieldParameters();
        fieldParameters.setFieldId(fieldId);
        imageParameters.setFieldParameters(fieldParameters);

        if (image != null)
            imageParameters.setImage(image.toDssDocument());

        if (text != null && !text.isBlank()) {
            var textParameters = new SignatureImageTextParameters();
            textParameters.setText(text);
            if (image != null)
                textParameters.setSignerTextPosition(SignerTextPosition.BOTTOM);
            imageParameters.setTextParameters(textParameters);
        }

        return imageParameters;
    }
}