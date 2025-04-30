package digital.slovensko.autogram.ui.gui;

import javafx.fxml.FXML;

import java.util.ResourceBundle;

public abstract class BaseController implements HasI18n {
    @FXML
    protected ResourceBundle resources; // must be called "resources" to inject

    @Override
    public ResourceBundle getResources() {
        return resources;
    }

    /**
     * This method is called after all the {@link FXML @FXML} elements have been injected.
     */
    public abstract void initialize();
}
