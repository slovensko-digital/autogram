package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchResponder;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class BatchMethodSelectionDialogController extends BaseController implements SuppressedFocusController {
    private final Batch batch;
    private final BatchResponder allAtOnceResponder;
    private final BatchResponder oneByOneResponder;
    private final Autogram autogram;

    @FXML
    VBox mainBox;
    @FXML
    public Button signAllButton;
    @FXML
    public Button signOneByOneButton;

    public BatchMethodSelectionDialogController(Batch batch, BatchResponder allAtOnceResponder,
            BatchResponder oneByOneResponder, Autogram autogram) {
        this.batch = batch;
        this.allAtOnceResponder = allAtOnceResponder;
        this.oneByOneResponder = oneByOneResponder;
        this.autogram = autogram;
    }

    @Override
    public void initialize() {
    }

    public void onSignAllButtonPressed(ActionEvent event) {
        close();
        autogram.startBatch(batch, allAtOnceResponder);
    }

    public void onSignOneByOneButtonPressed(ActionEvent event) {
        close();
        autogram.startOneByOneBatch(batch, oneByOneResponder);
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
