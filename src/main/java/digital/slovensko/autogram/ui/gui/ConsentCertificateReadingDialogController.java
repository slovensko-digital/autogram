package digital.slovensko.autogram.ui.gui;

import javafx.application.HostServices;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.util.function.Consumer;

public class ConsentCertificateReadingDialogController extends BaseController implements SuppressedFocusController {
    private final HostServices hostServices;
    private final Consumer<Runnable> callback;
    private final Runnable cancelCallback;

    @FXML
    VBox mainBox;

    public ConsentCertificateReadingDialogController(HostServices hostServices, Consumer<Runnable> callback, Runnable cancelCallback) {
        this.hostServices = hostServices;
        this.callback = callback;
        this.cancelCallback = cancelCallback;
    }

    @Override
    public void initialize() { }

    public void onContinueAction(ActionEvent event) {
        callback.accept(this::close);
    }

    public void onCancelAction(ActionEvent event) {
        cancelCallback.run();
        var stage = (Stage) mainBox.getScene().getWindow();
        stage.close();
    }

    public void close() {
        var stage = (Stage) mainBox.getScene().getWindow();
        stage.close();
    }

    @Override
    public Node getNodeForLoosingFocus() {
        return mainBox;
    }
}
