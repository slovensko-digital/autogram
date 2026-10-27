package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.CertificateStatusChecker;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SignatureRequirements;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.CertificateQualification;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static digital.slovensko.autogram.util.DSSUtils.buildTooltipLabel;
import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class PickKeyDialogController extends BaseController {
    private final Consumer<DSSPrivateKeyEntry> callback;
    private final List<DSSPrivateKeyEntry> keys;
    private final UserSettings userSettings;
    private final BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate;
    private final Runnable openSettings;
    private final TimestampOptions timestampOptions;
    private final Set<DSSPrivateKeyEntry> checkPending = new HashSet<>();
    private final Map<DSSPrivateKeyEntry, CertificateStatusChecker.Result> results = new LinkedHashMap<>();

    public record TimestampOptions(SigningJob job, TSPSource source) { }

    private record Choice(DSSPrivateKeyEntry key, boolean timestampRequested, HBox badges, Text description) { }

    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    VBox mainBox;
    @FXML
    VBox radios;
    @FXML
    VBox hint;
    @FXML
    Text hintText;
    @FXML
    Hyperlink settingsLink;
    @FXML
    Button mainButton;
    @FXML
    Text timestampStatus;
    private ToggleGroup toggleGroup;
    private String mainButtonText;
    private boolean trustedListsLoading;


    /**
     * @param openSettings opens the settings after this dialog closed itself, null when the settings are not available
     */
    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback, UserSettings userSettings,
            BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate, Runnable openSettings) {
        this(keys, callback, userSettings, checkCertificate, openSettings, null);
    }

    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback, UserSettings userSettings,
            BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate, Runnable openSettings,
            TimestampOptions timestampOptions) {
        this.keys = keys;
        this.callback = callback;
        this.userSettings = userSettings;
        this.checkCertificate = checkCertificate;
        this.openSettings = openSettings;
        this.timestampOptions = timestampOptions;
    }

    @Override
    public void initialize() {
        toggleGroup = new ToggleGroup();
        mainButtonText = mainButton.getText();
        var timestampRequested = canSelectTimestamp() || timestampOptions != null
                && timestampOptions.job().getParameters().getSignatureProfile() != SignatureProfile.BASELINE_B;
        for (var key : keys) {
            var expired = !key.getCertificate().isValidOn(new java.util.Date());
            if (expired) {
                if (!userSettings.isExpiredCertsEnabled())
                    continue;
            } else {
                checkPending.add(key);
            }

            if (canSelectTimestamp()) {
                addChoice(key, true, expired);
                addChoice(key, false, expired);
            } else {
                addChoice(key, timestampRequested, expired);
            }
            if (!expired)
                checkCertificate.accept(key, result -> showResult(key, result));
        }

        if (keys.size() == 1 && !toggleGroup.getToggles().isEmpty())
            toggleGroup.getToggles().stream()
                    .filter(toggle -> ((Choice) toggle.getUserData()).timestampRequested() == timestampRequested)
                    .filter(toggle -> !((RadioButton) toggle).isDisabled())
                    .findFirst().ifPresent(toggle -> toggle.setSelected(true));

        settingsLink.setVisible(openSettings != null);
        settingsLink.setManaged(openSettings != null);
        toggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            updateMainButton();
            resizeToContent();
        });
        updateMainButton();
    }

    private boolean canSelectTimestamp() {
        return timestampOptions != null && timestampOptions.job().canSelectTimestamp();
    }

    private void addChoice(DSSPrivateKeyEntry key, boolean timestampRequested, boolean expired) {
        var radio = new RadioButton();
        radio.getStyleClass().add("autogram-pick-key-choice");
        radio.setToggleGroup(toggleGroup);
        var name = new Text(parseCN(key.getCertificate().getSubject().getRFC2253()));
        name.getStyleClass().add("autogram-body");
        var badges = new HBox(expired ? SignatureBadgeFactory.createInfoBadge(i18n("pickKey.certificateStatus.expired"))
                : SignatureBadgeFactory.createInProgressBadge(i18n("pickKey.certificateStatus.checking")));
        badges.getStyleClass().add("autogram-pick-key-badges");
        var identity = new HBox(name, badges);
        identity.setMinWidth(Region.USE_PREF_SIZE);
        var description = new Text(i18n(timestampRequested ? "pickKey.timestamp.with" : "pickKey.timestamp.without"));
        description.getStyleClass().addAll("autogram-body-s", "autogram-pick-key-description");
        var choice = new Choice(key, timestampRequested, badges, description);
        radio.setUserData(choice);
        var content = new VBox(identity, description);
        content.getStyleClass().add("autogram-pick-key-option");
        radio.setGraphic(content);
        radio.setMaxWidth(Double.MAX_VALUE);
        radios.getChildren().add(radio);
        updateDescription(choice, null);
        radio.setAccessibleText(accessibleText(content));

        var tooltip = new Tooltip(buildTooltipLabel(key));
        tooltip.setShowDuration(Duration.seconds(10));
        tooltip.setWrapText(true);
        tooltip.setPrefWidth(400);
        radio.setTooltip(tooltip);
    }

    private void updateDescription(Choice choice, CertificateQualification qualification) {
        var visible = timestampOptions != null && qualification != CertificateQualification.QCERT_FOR_ESIG_QSCD;
        choice.description().setVisible(visible);
        choice.description().setManaged(visible);
    }

    private static String accessibleText(Node node) {
        if (!node.isVisible())
            return "";
        if (node instanceof Text text)
            return text.getText();
        if (node instanceof Parent parent)
            return parent.getChildrenUnmodifiable().stream().map(PickKeyDialogController::accessibleText)
                    .filter(text -> !text.isEmpty()).collect(Collectors.joining(", "));
        return "";
    }

    private void showResult(DSSPrivateKeyEntry key, CertificateStatusChecker.Result result) {
        checkPending.remove(key);
        results.put(key, result);
        toggleGroup.getToggles().forEach(toggle -> {
            var choice = (Choice) toggle.getUserData();
            if (choice.key() != key)
                return;
            choice.badges().getChildren().setAll(badgesFor(result, choice.timestampRequested()));
            updateDescription(choice, result.qualification());
            ((RadioButton) toggle).setAccessibleText(accessibleText(((RadioButton) toggle).getGraphic()));
            if (!isAllowed(result)) {
                toggle.setSelected(false);
                ((RadioButton) toggle).setDisable(true);
            }
        });

        if (checkPending.isEmpty())
            showHintIfNothingQualified();

        updateMainButton();
        resizeToContent();
    }

    private List<Node> badgesFor(CertificateStatusChecker.Result result, boolean timestampRequested) {
        var badges = new ArrayList<Node>();
        if (result.status() == CertificateStatus.REVOKED)
            badges.add(SignatureBadgeFactory.createInvalidBadge(i18n("pickKey.certificateStatus.revoked")));
        else if (result.status() != CertificateStatus.GOOD)
            badges.add(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.certificateStatus.unknown")));

        var qualificationBadge = canSelectTimestamp()
                ? SignatureBadgeFactory.createRequestedBadgeFromCertificateQualification(result.qualification(), timestampRequested, resources)
                : SignatureBadgeFactory.createBadgeFromCertificateQualification(result.qualification(), timestampRequested, null, resources);
        Tooltip.install(qualificationBadge, new Tooltip(i18n("pickKey.qualification.expectedLevel")));
        badges.add(qualificationBadge);

        return badges;
    }

    private boolean isAllowed(CertificateStatusChecker.Result result) {
        return result.status() != CertificateStatus.REVOKED
                && (!result.isNotQualified() || userSettings.isNonQualifiedCertsEnabled());
    }

    private void showHintIfNothingQualified() {
        if (results.isEmpty() || !results.values().stream().allMatch(CertificateStatusChecker.Result::isNotQualified))
            return;

        var locale = resources.getLocale();
        var countriesTurnedOff = results.keySet().stream()
                .map(key -> CertificateStatusChecker.issuerCountry(key.getCertificate()))
                .flatMap(Optional::stream)
                .map(Country::withTrustedList)
                .flatMap(Optional::stream)
                .filter(country -> !userSettings.getTrustedList().contains(country.getShortname()))
                .map(country -> country.getName(locale))
                .distinct()
                .toList();

        var text = countriesTurnedOff.isEmpty()
                ? i18n("pickKey.hint.notQualified")
                : i18n("pickKey.hint.countryTurnedOff", String.join(", ", countriesTurnedOff));
        if (!userSettings.isNonQualifiedCertsEnabled())
            text += " " + i18n("pickKey.hint.allowNotQualified");

        hintText.setText(text);
        hint.setVisible(true);
        hint.setManaged(true);
    }

    /** Tells the user why the checks take longer, e.g. right after Autogram started. */
    public void setTrustedListsLoading(boolean trustedListsLoading) {
        this.trustedListsLoading = trustedListsLoading;
        updateMainButton();
    }

    private void updateMainButton() {
        var checking = !checkPending.isEmpty();
        var nothingToPick = toggleGroup.getToggles().stream().allMatch(toggle -> ((RadioButton) toggle).isDisabled());
        var selected = toggleGroup.getSelectedToggle();
        var missingTimestampSource = timestampOptions != null && timestampOptions.source() == null
                && selected != null && ((Choice) selected.getUserData()).timestampRequested();
        timestampStatus.setText(missingTimestampSource ? i18n("pickKey.timestamp.missingSource") : "");
        timestampStatus.getParent().setVisible(missingTimestampSource);
        timestampStatus.getParent().setManaged(missingTimestampSource);
        mainButton.setDisable(checking || nothingToPick || missingTimestampSource);
        if (!checking)
            mainButton.setText(mainButtonText);
        else if (trustedListsLoading)
            mainButton.setText(i18n("pickKey.trustedListsLoading.btn"));
        else
            mainButton.setText(i18n("pickKey.certificateStatus.checking.btn"));
    }

    private void resizeToContent() {
        if (mainBox.getScene() != null && mainBox.getScene().getWindow() != null)
            mainBox.getScene().getWindow().sizeToScene();
    }

    public void onOpenSettingsAction(ActionEvent actionEvent) {
        GUIUtils.closeWindow(mainBox);
        openSettings.run();
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        if (mainButton.isDisabled())
            return;

        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            var choice = (Choice) toggleGroup.getSelectedToggle().getUserData();
            if (canSelectTimestamp()) {
                timestampOptions.job().setTimestampEnabled(choice.timestampRequested(), timestampOptions.source());
                var result = results.get(choice.key());
                timestampOptions.job().setSignatureRequirements(SignatureRequirements.fromCertificate(
                        result == null ? null : result.qualification(), choice.timestampRequested()));
            }
            GUIUtils.closeWindow(mainBox);
            callback.accept(choice.key());
        }
    }
}
