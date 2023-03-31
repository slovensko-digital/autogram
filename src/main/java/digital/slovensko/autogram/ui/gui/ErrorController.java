package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.AutogramException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class ErrorController {
    private final AutogramException exception;
    @FXML
    VBox mainBox;

    @FXML
    Label heading;

    @FXML
    Label subheading;

    @FXML
    Label description;

    public ErrorController(AutogramException e) {
        this.exception = e;
    }

    public void initialize() {
        // TODO actually load these from i18n
        heading.setText(exception.getHeading());
        subheading.setText(exception.getSubheading());
        description.setText(exception.getDescription());
    }

    public void onMainButtonAction() {
        ((Stage) mainBox.getScene().getWindow()).close(); // TODO refactor
    }
}
