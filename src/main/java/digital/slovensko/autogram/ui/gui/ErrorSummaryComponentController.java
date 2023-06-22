package digital.slovensko.autogram.ui.gui;

import java.io.PrintWriter;
import java.io.StringWriter;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class ErrorSummaryComponentController {

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

    private AutogramException exception;

    public ErrorSummaryComponentController() {
    }

    public void setException(AutogramException e) {
        this.exception = e;

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

    public void initialize() {
    }

    private String formatException(AutogramException exception) {
        var writer = new StringWriter();
        var printWriter = new PrintWriter(writer);
        exception.printStackTrace(printWriter);
        printWriter.flush();

        return writer.toString();
    }

    public void onShowErrorDetailsButtonAction() {
        errorDetails.setText(formatException(exception));
        if (errorDetails.isVisible()) {
            errorDetails.setManaged(false);
            errorDetails.setVisible(false);
            showErrorDetailsButton.getStyleClass().remove("autogram-error-summary__more-open");
            showErrorDetailsButton.setText("Zobraziť detail chyby");
        } else {
            errorDetails.setManaged(true);
            errorDetails.setVisible(true);
            showErrorDetailsButton.getStyleClass().add("autogram-error-summary__more-open");
            showErrorDetailsButton.setText("Schovať detail chyby");
        }
        errorDetails.getScene().getWindow().sizeToScene();
    }
}
