package digital.slovensko.autogram.ui.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import digital.slovensko.autogram.core.SigningJob;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;

public class IgnorableExceptionDialogController implements SuppressedFocusController {
    private final SigningJob job;
    private final Exception exception;
    private final GUI gui;

    @FXML
    Button cancelButton;
    @FXML
    Button continueButton;
    @FXML
    Node mainBox;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    public IgnorableExceptionDialogController(SigningJob job, Exception exception, GUI gui) {
        this.job = job;
        this.exception = exception;
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

    public void onShowErrorDetailsButtonAction() {
        errorDetails.setText(formatException(exception));
        if (errorDetails.isVisible()) {
            errorDetails.setManaged(false);
            errorDetails.setVisible(false);
            showErrorDetailsButton.lookup("Polygon").setRotate(0);
            showErrorDetailsButton.setText("Zobraziť detail chyby");
        } else {
            errorDetails.setManaged(true);
            errorDetails.setVisible(true);
            showErrorDetailsButton.lookup("Polygon").setRotate(90);
            showErrorDetailsButton.setText("Schovať detail chyby");
        }
        errorDetails.getScene().getWindow().sizeToScene();
    }

    private String formatException(Exception exception) {
        var writer = new StringWriter();
        var printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        printWriter.flush();

        return writer.toString();
    }
}
