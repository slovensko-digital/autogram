package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.ISigningJob;
import digital.slovensko.autogram.core.SigningJob;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PDFAComplianceDialogController implements SuppressedFocusController {
    private final ISigningJob job;
    private final GUI gui;

    @FXML
    Button cancelButton;
    @FXML
    Button continueButton;
    @FXML
    Node mainBox;

    public PDFAComplianceDialogController(ISigningJob job, GUI gui) {
        this.job = job;
        this.gui = gui;
    }

    public void onCancelAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
        gui.cancelJob(job);
    }

    public void onContinueAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
        gui.focusJob(job);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
