package digital.slovensko.autogram.ui.gui;

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
            errorDetails.setText(GUIUtils.exceptionToString(exception));
            showErrorDetailsButton.setVisible(true);
        }
    }

    public void disableErrorDetails() {
        showErrorDetailsButton.setVisible(false);
        showErrorDetailsButton.setManaged(false);
    }

    public void initialize() {
    }

    public void onShowErrorDetailsButtonAction() {
        errorDetails.setText(GUIUtils.exceptionToString(exception));
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
