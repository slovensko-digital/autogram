package digital.slovensko.autogram.ui.gui;

import eu.europa.esig.dss.validation.reports.Reports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.*;

public class SignaturesInvalidDialogController implements SuppressedFocusController {
    private final SigningDialogController signingDialogController;

    @FXML
    Button cancelButton;
    @FXML
    Button continueButton;
    @FXML
    Node mainBox;
    @FXML
    VBox signaturesTable;

    public SignaturesInvalidDialogController(SigningDialogController signigDialogController) {
        this.signingDialogController = signigDialogController;
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage)
            ((Stage) window).close();

        signingDialogController.enableSigning();
    }

    public void onCancelAction() {
        close();
        signingDialogController.close();
    }

    public void onContinueAction() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage)
            ((Stage) window).close();
        signingDialogController.sign();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

    public void initialize(Reports signatureValidationReports) {
        signaturesTable.getChildren().clear();
        signaturesTable.getChildren().addAll(
                createSignatureTableRows(signatureValidationReports, true, e -> {
                    signingDialogController.onShowSignaturesButtonPressed(null);
                }));
    }
}
