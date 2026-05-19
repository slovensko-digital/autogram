package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PkcsEidWindowsDllErrorController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;

    @FXML
    VBox mainBox;

    public PkcsEidWindowsDllErrorController(HostServices hostServices) {
        this.hostServices = hostServices;
    }

    @Override
    public void initialize() { }

    public void downloadAction(ActionEvent ignored) {
        hostServices.showDocument("https://sluzby.slovensko.digital/autogram/vc-redist-redirect");
    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
