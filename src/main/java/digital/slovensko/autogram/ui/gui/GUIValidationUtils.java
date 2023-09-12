package digital.slovensko.autogram.ui.gui;

import java.util.ArrayList;
import java.util.function.Consumer;

import eu.europa.esig.dss.enumerations.SignatureQualification;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.validation.reports.Reports;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public class GUIValidationUtils {
    public static HBox createSignatureTableHeader(boolean isValidated) {
        var whoSignedText = new Text("Podpísal");
        whoSignedText.getStyleClass().add("autogram-heading-s");
        var whoSigned = new TextFlow(whoSignedText);
        whoSigned.setPrefWidth(360);

        var signatureTypeText = new Text("Typ podpisu");
        signatureTypeText.getStyleClass().add("autogram-heading-s");
        var signatureType = new TextFlow(signatureTypeText);

        var r = new HBox(whoSigned, signatureType);
        r.getStyleClass().add("autogram-table-cell");
        return r;
    }

    public static VBox createSignatureTableRows(Reports reports, boolean isValidated, Consumer<String> callback) {
        var r = new VBox();
        for (var signatureId : reports.getSimpleReport().getSignatureIdList()) {
            var name = reports.getSimpleReport().getSignedBy(signatureId);
            var signatureType = reports.getDetailedReport().getSignatureQualification(signatureId);
            var valid = reports.getSimpleReport().isValid(signatureId);

            var timestampQualifications = new ArrayList<TimestampQualification>();
            for (var timestampId : reports.getSimpleReport().getSignatureTimestamps(signatureId))
                timestampQualifications.add(reports.getDetailedReport().getTimestampQualification(timestampId.getId()));

            r.getChildren().add(createSignatureTableRow(name, signatureType, timestampQualifications, isValidated, valid, callback));
        }

        return r;
    }

    public static HBox createSignatureTableRow(String name, SignatureQualification signatureType, ArrayList<TimestampQualification> timestampQualifications, boolean isValidated, boolean valid, Consumer<String> callback) {
        var whoSignedButton = new Button(name);
        whoSignedButton.getStyleClass().addAll("autogram-link");
        whoSignedButton.setPrefWidth(360);
        whoSignedButton.setOnMouseClicked(event -> {
            callback.accept(null);
        });

        var whoSignedBox = new HBox(whoSignedButton);
        whoSignedBox.setStyle("-fx-padding: 0.25em 0;");

        var signatureTypeBox = SignatureBadgeFactory.createCombinedBadgeFromQualification(isValidated ? signatureType : null, timestampQualifications);
        if (isValidated && !valid)
            signatureTypeBox = SignatureBadgeFactory.createInvalidBadge("Neplatný podpis");

        var r = new HBox(whoSignedBox, new VBox(signatureTypeBox));
        r.getStyleClass().add("autogram-table-cell");
        return r;
    }

}
