package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public class IgnorableExceptionDialogController implements SuppressedFocusController {
    private final IgnorableException exception;

    @FXML
    VBox mainBox;

    @FXML
    Button cancelButton;

    @FXML
    Button continueButton;

    @FXML
    ErrorSummaryComponentController errorSummaryComponentController;

    public IgnorableExceptionDialogController(IgnorableException exception) {
        this.exception = exception;
    }

    public void initialize() {
        errorSummaryComponentController.setException(exception);
    }

    public void onCancelAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
        exception.getJob().onDocumentSignFailed(new SigningCanceledByUserException());
    }

    public void onContinueAction(ActionEvent ignored) {
        GUIUtils.closeWindow(mainBox);
        exception.getOnContinueCallback().run();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }

}
