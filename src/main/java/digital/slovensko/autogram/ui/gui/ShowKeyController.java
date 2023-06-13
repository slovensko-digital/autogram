package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.util.DSSUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import javax.naming.InvalidNameException;

public class ShowKeyController implements SuppressedFocusController {
    @FXML
    VBox mainBox;

    @FXML
    Text heading;

    @FXML
    Text subheading;

    @FXML
    Text description;

    @FXML
    Button showErrorDetailsButton;

    GUI gui;

    public ShowKeyController(GUI gui) {
        this.gui = gui;
    }

    public void initialize() {
        var key = this.gui.getActiveSigningKey();
        subheading.setText(DSSUtils.parseCN(key.getCertificate().getSubject().getRFC2253()));
        try {
            var cert = key.getCertificate();
            description.setText("""
                    DN: %s

                    Subject: %s

                    Issuer: %s
                    """.formatted(key.prettyPrintCertificateDetails(),
                    cert.getSubject().getCanonical(), cert.getIssuer().getCanonical()));
        } catch (InvalidNameException e) {
            description.setText("Nepodarilo sa získať detaily certifikátu.");
        }

    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
