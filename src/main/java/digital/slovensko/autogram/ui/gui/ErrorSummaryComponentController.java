package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;

public class ErrorSummaryComponentController extends BaseController {

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

        heading.setText(exception.getHeading(resources));
        var subheading = exception.getSubheading(resources);
        this.subheading.setText(subheading);
        if (subheading == null || subheading.isBlank()) {
            this.subheading.setManaged(false);
            this.subheading.setVisible(false);
        }
        description.setText(exception.getDescription(resources));
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
            showErrorDetailsButton.setText(i18n("error.details.show.btn"));
        } else {
            errorDetails.setManaged(true);
            errorDetails.setVisible(true);
            showErrorDetailsButton.getStyleClass().add("autogram-error-summary__more-open");
            showErrorDetailsButton.setText(i18n("error.details.hide.btn"));
        }
        errorDetails.getScene().getWindow().sizeToScene();
    }
}
