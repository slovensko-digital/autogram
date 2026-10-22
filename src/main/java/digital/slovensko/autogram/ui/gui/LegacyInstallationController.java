package digital.slovensko.autogram.ui.gui;

import java.io.IOException;
import java.util.List;

import digital.slovensko.autogram.core.LegacyInstallation;
import digital.slovensko.autogram.util.Logging;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import static digital.slovensko.autogram.ui.gui.TextHighlighter.highlight;
import static java.util.stream.Collectors.joining;

public class LegacyInstallationController extends BaseController implements SuppressedFocusController {
    private final List<LegacyInstallation> installations;
    private final GUI gui;
    @FXML
    Node mainBox;
    @FXML
    TextFlow title;
    @FXML
    Text text;
    @FXML
    Text error;
    @FXML
    Button mainButton;
    @FXML
    Button cancelButton;

    public LegacyInstallationController(List<LegacyInstallation> installations, GUI gui) {
        this.installations = installations;
        this.gui = gui;
    }

    @Override
    public void initialize() {
        highlight("Autogram").in(title);
        var versions = installations.stream().map(LegacyInstallation::version).collect(joining(", "));
        text.setText(i18n("legacyInstallation.text", versions));
    }

    public void onUninstallButtonPressed(ActionEvent ignored) {
        mainButton.setDisable(true);
        cancelButton.setDisable(true);
        mainButton.setText(i18n("legacyInstallation.uninstalling.btn"));
        error.setManaged(false);
        error.setVisible(false);

        gui.onWorkThreadDo(() -> {
            String failedExitCode = null;
            for (var installation : installations) {
                try {
                    var exitCode = installation.uninstall();
                    if (!LegacyInstallation.isUninstalled(exitCode))
                        failedExitCode = String.valueOf(exitCode);
                } catch (IOException | InterruptedException e) {
                    Logging.log("Unable to uninstall " + installation + ": " + e);
                    failedExitCode = e.getClass().getSimpleName();
                }
            }

            var result = failedExitCode;
            gui.onUIThreadDo(() -> onUninstallFinished(result));
        });
    }

    private void onUninstallFinished(String failedExitCode) {
        if (failedExitCode == null) {
            GUIUtils.closeWindow(mainBox);
            return;
        }

        error.setText(i18n("legacyInstallation.failed", failedExitCode));
        error.setManaged(true);
        error.setVisible(true);
        mainButton.setText(i18n("legacyInstallation.uninstall.btn"));
        mainButton.setDisable(false);
        cancelButton.setDisable(false);
        mainBox.getScene().getWindow().sizeToScene();
    }

    public void onCancelButtonPressed(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
