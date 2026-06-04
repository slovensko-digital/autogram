package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.ValidationReports;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import static digital.slovensko.autogram.ui.gui.GUIValidationUtils.createSignatureTableRows;

public class SignaturesInvalidDialogController extends BaseController implements SuppressedFocusController {
    private final SigningDialogController signingDialogController;
    private final ValidationReports reports;

    @FXML
    Button cancelButton;
    @FXML
    Button continueButton;
    @FXML
    Node mainBox;
    @FXML
    VBox signaturesTable;

    public SignaturesInvalidDialogController(SigningDialogController controller, ValidationReports reports) {
        this.signingDialogController = controller;
        this.reports = reports;
    }

    public void initialize() {
        signaturesTable.getChildren().clear();
        signaturesTable.getChildren().addAll(
                createSignatureTableRows(resources, reports, true, e -> {
                    signingDialogController.onShowSignaturesButtonPressed(null);
                }, 6));
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage)
            ((Stage) window).close();

        signingDialogController.enableSigningOnAllJobs();
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
}
