package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorController implements SuppressedFocusController {
    private final AutogramException exception;

    @FXML
    VBox mainBox;

    @FXML
    ErrorSummaryComponentController errorSummaryComponentController; // this is {include fx:id} + "Controller"

    public ErrorController(AutogramException e) {
        this.exception = e;
    }

    public void initialize() {
        errorSummaryComponentController.setException(exception);
    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
