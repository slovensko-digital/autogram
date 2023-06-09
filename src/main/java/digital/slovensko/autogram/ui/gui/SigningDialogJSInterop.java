package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.VisualSignature;
import eu.europa.esig.dss.pades.SignatureFieldParameters;

public class SigningDialogJSInterop {
    VisualSignature signature;

    public void setSignaturePosition(int pageNumber, int x, int y, int width, int height) {
        signature = new VisualSignature(pageNumber, x, y, width, height);
        System.out.println("Signature position: " + signature.toString());
    }

    public SignatureFieldParameters getSignatureFieldParameters() {
        var parameters = new SignatureFieldParameters();
        parameters.setPage(signature.pageNumber());
        parameters.setOriginX(signature.x());
        parameters.setOriginY(signature.y());
        parameters.setWidth(signature.width());
        parameters.setHeight(signature.height());
        return parameters;
    }

    public void setSignatureImage(String image) {
        System.out.println("Signature image: " + image);
    }

    public void log(String message) {
        System.out.println("JS: " + message);
    }
}
