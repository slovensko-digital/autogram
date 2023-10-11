package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class ErrorBeforeMainMenuController implements SuppressedFocusController {
    private final GUI gui;
    private final Stage widowStage;
    private final Autogram autogram;
    private final int portNumber;

    @FXML
    VBox mainBox;

    @FXML
    Text textField;

    public ErrorBeforeMainMenuController(GUI gui, Stage widowStage, Autogram autogram, int portNumber) {
        this.gui = gui;
        this.autogram = autogram;
        this.widowStage = widowStage;
        this.portNumber = portNumber;
    }

    public void initialize() {
        textField.setText("Nepodarilo sa spojiť so serverom. Port: " + portNumber + " sa už používa.");
    }

    public void onContinueAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
        gui.showMainMenu(widowStage, autogram);
    }

    public void onCancelAction() {
        widowStage.fireEvent(new WindowEvent(widowStage, WindowEvent.WINDOW_CLOSE_REQUEST));
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
