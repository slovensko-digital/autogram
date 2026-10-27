package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.CertificateStatusChecker;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.TimestampSourceChecker;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.enumerations.SignatureProfile;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.spi.x509.tsp.TSPSource;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
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
    private final Map<DSSPrivateKeyEntry, HBox> badgesByKey = new LinkedHashMap<>();
    private TimestampSourceChecker.Result timestampResult;
    private boolean timestampCheckStarted;
    private boolean timestampChecking;

    public record TimestampOptions(SigningJob job, TSPSource source,
            Consumer<Consumer<TimestampSourceChecker.Result>> checkSource) { }

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
    VBox timestampControls;
    @FXML
    CheckBox addTimestamp;
    @FXML
    Text timestampHint;
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
        for (var key : keys) {
            Node badge = new HBox();
            var expired = !key.getCertificate().isValidOn(new java.util.Date());
            if (expired) {
                badge = SignatureBadgeFactory.createInfoBadge("Exspirovaný certifikát");

                if (!userSettings.isExpiredCertsEnabled())
                    continue;
            } else {
                badge = SignatureBadgeFactory.createInProgressBadge(i18n("pickKey.certificateStatus.checking"));
                checkPending.add(key);
            }

            var radioButton = new RadioButton(parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            var badges = new HBox(badge);
            badgesByKey.put(key, badges);
            badges.getStyleClass().add("autogram-pick-key-badges");
            radios.getChildren().add(new HBox(radioButton, badges));

            var tooltip = new Tooltip(buildTooltipLabel(key));
            tooltip.setShowDuration(Duration.seconds(10));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(400);
            radioButton.setTooltip(tooltip);

            if (!expired)
                checkCertificate.accept(key, result -> showResult(key, radioButton, badges, result));
        }

        if (keys.size() == 1)
            toggleGroup.getToggles().get(0).setSelected(true);

        settingsLink.setVisible(openSettings != null);
        settingsLink.setManaged(openSettings != null);
        initializeTimestamp();
        updateMainButton();
    }

    private void initializeTimestamp() {
        timestampControls.setVisible(timestampOptions != null);
        timestampControls.setManaged(timestampOptions != null);
        if (timestampOptions == null)
            return;

        addTimestamp.setSelected(timestampOptions.job().getParameters().getSignatureProfile() == SignatureProfile.BASELINE_T);
        addTimestamp.setDisable(!timestampOptions.job().canSelectTimestamp());
        timestampHint.setText(i18n(timestampOptions.job().canSelectTimestamp() ? "pickKey.timestamp.hint" : "pickKey.timestamp.fixed"));
        addTimestamp.selectedProperty().addListener((observable, oldValue, newValue) -> updateTimestamp());
        updateTimestamp();
    }

    private void updateTimestamp() {
        if (addTimestamp.isSelected() && !timestampCheckStarted && timestampOptions.source() != null) {
            timestampCheckStarted = true;
            timestampChecking = true;
            timestampOptions.checkSource().accept(result -> {
                timestampChecking = false;
                timestampResult = result;
                updateTimestamp();
            });
        }

        var qualification = timestampResult == null ? null : timestampResult.qualification();
        addTimestamp.setText(i18n(qualification == TimestampQualification.QTSA ? "pickKey.timestamp.addQualified" : "pickKey.timestamp.add"));
        var statusKey = timestampStatusKey();
        timestampStatus.setText(statusKey == null ? "" : i18n(statusKey));
        timestampStatus.getParent().setManaged(statusKey != null);
        timestampStatus.getParent().setVisible(statusKey != null);
        results.forEach((key, result) -> badgesByKey.get(key).getChildren().setAll(badgesFor(result)));
        updateMainButton();
        resizeToContent();
    }

    private String timestampStatusKey() {
        if (!addTimestamp.isSelected())
            return null;
        if (timestampOptions.source() == null)
            return "pickKey.timestamp.missingSource";
        if (timestampChecking)
            return "pickKey.timestamp.checking";
        if (timestampResult == null || timestampResult.qualification() == null)
            return "pickKey.timestamp.unknown";
        return switch (timestampResult.qualification()) {
            case QTSA -> "pickKey.timestamp.qualified";
            case TSA -> "pickKey.timestamp.notQualified";
            default -> "pickKey.timestamp.unknown";
        };
    }

    private void showResult(DSSPrivateKeyEntry key, RadioButton radioButton, HBox badges, CertificateStatusChecker.Result result) {
        checkPending.remove(key);
        results.put(key, result);
        badges.getChildren().setAll(badgesFor(result));

        if (!isAllowed(result)) {
            radioButton.setSelected(false);
            radioButton.setDisable(true);
        }

        if (checkPending.isEmpty())
            showHintIfNothingQualified();

        updateMainButton();
        resizeToContent();
    }

    private List<Node> badgesFor(CertificateStatusChecker.Result result) {
        var badges = new ArrayList<Node>();
        if (result.status() == CertificateStatus.REVOKED)
            badges.add(SignatureBadgeFactory.createInvalidBadge(i18n("pickKey.certificateStatus.revoked")));
        else if (result.status() != CertificateStatus.GOOD)
            badges.add(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.certificateStatus.unknown")));

        var qualificationBadge = SignatureBadgeFactory.createBadgeFromCertificateQualification(result.qualification(),
                timestampOptions != null && addTimestamp.isSelected(), timestampResult == null ? null : timestampResult.qualification(), resources);
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
        var checkingTimestamp = timestampOptions != null && addTimestamp.isSelected() && timestampChecking;
        var nothingToPick = toggleGroup.getToggles().stream().allMatch(toggle -> ((RadioButton) toggle).isDisabled());
        var missingTimestampSource = timestampOptions != null && addTimestamp.isSelected() && timestampOptions.source() == null;
        mainButton.setDisable(checking || checkingTimestamp || nothingToPick || missingTimestampSource);
        if (!checking && checkingTimestamp)
            mainButton.setText(i18n("pickKey.timestamp.checking"));
        else if (!checking)
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
            if (timestampOptions != null && timestampOptions.job().canSelectTimestamp())
                timestampOptions.job().setTimestampEnabled(addTimestamp.isSelected(),
                        timestampResult == null ? timestampOptions.source() : timestampResult.source());
            GUIUtils.closeWindow(mainBox);
            var key = (DSSPrivateKeyEntry) toggleGroup.getSelectedToggle().getUserData();
            callback.accept(key);
        }
    }
}
