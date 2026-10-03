package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.errors.AutogramException;
import javafx.application.HostServices;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.util.regex.Pattern;

public class ErrorSummaryComponentController extends BaseController {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[\\w.+-]+@[\\w-]+(\\.[\\w-]+)*\\.[A-Za-z]{2,}");

    @FXML
    Text heading;

    @FXML
    Text subheading;

    @FXML
    TextFlow description;

    @FXML
    TextArea errorDetails;

    @FXML
    Button showErrorDetailsButton;

    private AutogramException exception;
    private HostServices hostServices;

    public ErrorSummaryComponentController() {
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
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
        setDescription(exception.getDescription(resources));
        if (exception.getCause() != null) {
            errorDetails.setText(GUIUtils.exceptionToString(exception));
            showErrorDetailsButton.setVisible(true);
        }
    }

    private void setDescription(String text) {
        description.getChildren().clear();
        if (text == null)
            return;

        var matcher = EMAIL_PATTERN.matcher(text);
        var lastEnd = 0;
        while (matcher.find()) {
            if (matcher.start() > lastEnd)
                description.getChildren().add(new Text(text.substring(lastEnd, matcher.start())));

            description.getChildren().add(createEmailLink(matcher.group()));
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length())
            description.getChildren().add(new Text(text.substring(lastEnd)));
    }

    private Hyperlink createEmailLink(String email) {
        var link = new Hyperlink(email);
        link.getStyleClass().addAll("autogram-link", "autogram-error-summary__email");
        link.setOnAction(e -> {
            if (hostServices != null)
                hostServices.showDocument("mailto:" + email);
        });

        var copyItem = new MenuItem(i18n("error.email.copy"));
        copyItem.setOnAction(e -> GUIUtils.copyToClipboard(email));
        link.setContextMenu(new ContextMenu(copyItem));

        return link;
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
