package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.SigningMode;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class PickBatchModeDialogController extends BaseController implements SuppressedFocusController {
    private final Consumer<SigningMode> onSelected;
    private final Runnable onCancel;

    @FXML
    VBox mainBox;
    @FXML
    public Button continueButton;
    @FXML
    public RadioButton signOneByOneButton;
    @FXML
    public RadioButton signAllButton;
    @FXML
    private final ToggleGroup signingMode = new ToggleGroup();

    public PickBatchModeDialogController(Consumer<SigningMode> onSelected, Runnable onCancel) {
        this.onSelected = onSelected;
        this.onCancel = onCancel;
    }

    @Override
    public void initialize() {
        signOneByOneButton.setToggleGroup(signingMode);
        signAllButton.setToggleGroup(signingMode);
        signOneByOneButton.setSelected(true);
    }

    public void onContinueButtonPressed() {
        close();
        onSelected.accept(signOneByOneButton.isSelected() ? SigningMode.INTERACTIVE : SigningMode.AUTOMATED);
    }

    public Runnable getOnCancel() {
        return onCancel;
    }

    public void close() {
        var window = mainBox.getScene().getRoot().getScene().getWindow();
        if (window instanceof Stage) {
            ((Stage) window).close();
        }
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
