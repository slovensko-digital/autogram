package digital.slovensko.autogram.ui.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class IgnorableExceptionDialogController implements SuppressedFocusController {
    private final IgnorableException exception;

    @FXML
    VBox mainBox;

    @FXML
    Text heading;

    @FXML
    Text subheading;

    @FXML
    Text description;

    @FXML
    Button cancelButton;

    @FXML
    Button continueButton;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    public IgnorableExceptionDialogController(IgnorableException exception) {
        this.exception = exception;
    }

    public void initialize() {
        // TODO actually load these from i18n
        heading.setText(exception.getHeading());
        subheading.setText(exception.getSubheading());
        if (exception.getSubheading() == null) {
            subheading.setManaged(false);
            subheading.setVisible(false);
        }
        description.setText(exception.getDescription());
        if (exception.getCause() != null) {
            errorDetails.setText(formatException(exception));
            showErrorDetailsButton.setVisible(true);
        }
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

    public void onShowErrorDetailsButtonAction() {
        errorDetails.setText(formatException(exception));
        if (errorDetails.isVisible()) {
            errorDetails.setManaged(false);
            errorDetails.setVisible(false);
            showErrorDetailsButton.lookup("Polygon").setRotate(0); // TODO - we need to do this in css
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
