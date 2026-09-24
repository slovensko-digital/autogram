package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SigningJob;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PDFAComplianceDialogController extends BaseController implements SuppressedFocusController {
    private final SigningJob job;
    private final GUI gui;
    private final Runnable onCancel;

    @FXML
    Button continueButton;
    @FXML
    Node mainBox;
    @FXML
    Button cancelButton;

    public PDFAComplianceDialogController(SigningJob job, GUI gui, Runnable onCancel) {
        this.job = job;
        this.gui = gui;
        this.onCancel = onCancel;
    }

    @Override
    public void initialize() { }

    public void onCancelAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
        onCancel.run();
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
