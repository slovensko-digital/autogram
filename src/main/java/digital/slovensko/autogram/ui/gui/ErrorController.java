package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.PrintWriter;
import java.io.StringWriter;

public class ErrorController implements SuppressedFocusController {
    private final AutogramException exception;
    @FXML
    VBox mainBox;

    @FXML
    Text heading;

    @FXML
    Text subheading;

    @FXML
    Text description;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    public ErrorController(AutogramException e) {
        this.exception = e;
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

    private String formatException(AutogramException exception) {
        var writer = new StringWriter();
        var printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        printWriter.flush();

        return writer.toString();
    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close();
    }

    public void onShowErrorDetailsButtonAction() {
        if (errorDetails.isVisible()) {
            errorDetails.setManaged(false);
            errorDetails.setVisible(false);
            showErrorDetailsButton.setText("Zobraziť detail chyby");
        } else {
            errorDetails.setManaged(true);
            errorDetails.setVisible(true);
            showErrorDetailsButton.setText("Schovať detail chyby");
        }
        errorDetails.getScene().getWindow().sizeToScene();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
