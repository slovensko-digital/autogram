package digital.slovensko.autogram.server.dto;

import java.util.Base64;
import eu.europa.esig.dss.enumerations.ImageScaling;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.pades.SignatureFieldParameters;
import eu.europa.esig.dss.pades.SignatureImageParameters;

public class ServerSignatureImageParameters {
    private final ServerSignatureFieldParameters signatureFieldParameters;
    private final String image;
    // private final Color backgroundColor;
    // private final int dpi
    // private final int zoom // 1-100
    // private final ImageScaling imageScaling
    // private final VisualSignatureAlignmentVertical alignmentVertical
    // private final VisualSignatureAlignmentHorizontal alignmentHorizontal
    // private final SignatureTextParameters textParameters

    public ServerSignatureImageParameters(String image,
            ServerSignatureFieldParameters signatureFieldParameters) {
        // this.backgroundColor = Color.decode(backgroundColor);
        this.image = image;
        this.signatureFieldParameters = signatureFieldParameters;
    }


    public SignatureImageParameters getSignatureImageParameters() {
        var params = new SignatureImageParameters();
        params.setImage(new InMemoryDocument(Base64.getDecoder().decode(image)));
        params.setFieldParameters(signatureFieldParameters.getSignatureFieldParameters());
        params.setImageScaling(ImageScaling.ZOOM_AND_CENTER);
        return params;
    }

    private record ServerSignatureFieldParameters(int page, float originX, float originY,
            float width, float height) {

        public SignatureFieldParameters getSignatureFieldParameters() {
            var params = new SignatureFieldParameters();
            params.setPage(page);
            params.setOriginX(originX);
            params.setOriginY(originY);
            params.setWidth(width);
            params.setHeight(height);
            return params;
        }
    }

}
