package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SigningJob;
import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;

public class PDFAComplianceDialogController extends BaseController implements SuppressedFocusController {
    private final SigningJob job;
    private final GUI gui;
    private final HostServices hostServices;

    @FXML
    Button continueButton;
    @FXML
    Node mainBox;
    @FXML
    Button cancelButton;

    public PDFAComplianceDialogController(SigningJob job, GUI gui, HostServices hostServices) {
        this.job = job;
        this.gui = gui;
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() { }

    public void onDocsAction(ActionEvent ignored) {
        GUIUtils.openDocs(hostServices, resources, i18n("pdfaCompliance.help"));
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
