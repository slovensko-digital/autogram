package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.BatchResponder;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class PickBatchModeDialogController extends BaseController implements SuppressedFocusController {
    private final int totalNumberOfDocuments;
    private final BatchResponder allAtOnceResponder;
    private final BatchResponder oneByOneResponder;
    private final Autogram autogram;

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

    public PickBatchModeDialogController(int totalNumberOfDocuments, BatchResponder allAtOnceResponder,
            BatchResponder oneByOneResponder, Autogram autogram) {
        this.totalNumberOfDocuments = totalNumberOfDocuments;
        this.allAtOnceResponder = allAtOnceResponder;
        this.oneByOneResponder = oneByOneResponder;
        this.autogram = autogram;
    }

    @Override
    public void initialize() {
        signOneByOneButton.setToggleGroup(signingMode);
        signAllButton.setToggleGroup(signingMode);
        signOneByOneButton.setSelected(true);
    }

    public void onContinueButtonPressed() {
        close();
        if (signOneByOneButton.isSelected())
            autogram.batchStartOneByOne(totalNumberOfDocuments, oneByOneResponder);
        else
            autogram.batchStart(totalNumberOfDocuments, allAtOnceResponder);
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
