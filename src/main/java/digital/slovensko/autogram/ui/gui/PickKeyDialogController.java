package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.CertificateStatusChecker;
import digital.slovensko.autogram.core.CertificateStatusChecker.Qualification;
import digital.slovensko.autogram.core.SigningCertificateType;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
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
    private final BiConsumer<DSSPrivateKeyEntry, SigningCertificateType> callback;
    private final List<DSSPrivateKeyEntry> keys;
    private final Set<SigningCertificateType> allowedTypes;
    private final UserSettings userSettings;
    private final BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate;
    private final Runnable openSettings;
    private final Set<DSSPrivateKeyEntry> checkPending = new HashSet<>();
    private final Map<DSSPrivateKeyEntry, CertificateStatusChecker.Result> results = new LinkedHashMap<>();

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
    private ToggleGroup toggleGroup;
    private String mainButtonText;
    private boolean trustedListsLoading;


    /**
     * @param allowedTypes types of certificates the user can pick, empty for any
     * @param callback gets the picked key and its certificate's type, null when unknown
     * @param openSettings opens the settings after this dialog closed itself, null when the settings are not available
     */
    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, Set<SigningCertificateType> allowedTypes,
            BiConsumer<DSSPrivateKeyEntry, SigningCertificateType> callback, UserSettings userSettings,
            BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate, Runnable openSettings) {
        this.keys = keys;
        this.allowedTypes = allowedTypes;
        this.callback = callback;
        this.userSettings = userSettings;
        this.checkCertificate = checkCertificate;
        this.openSettings = openSettings;
    }

    @Override
    public void initialize() {
        toggleGroup = new ToggleGroup();
        mainButtonText = mainButton.getText();
        for (var key : keys) {
            var badges = new HBox();
            badges.getStyleClass().add("autogram-pick-key-badges");
            var expired = !key.getCertificate().isValidOn(new java.util.Date());
            if (expired) {
                badges.getChildren().add(SignatureBadgeFactory.createInfoBadge("Exspirovaný certifikát"));

                if (!userSettings.isExpiredCertsEnabled())
                    continue;
            } else {
                checkPending.add(key);
            }

            var radioButton = new RadioButton(parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            // an expired certificate is not checked, so its type stays unknown
            radioButton.setDisable(expired && !allowedTypes.isEmpty());
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
        updateMainButton();
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
            showHintIfNothingFits();

        updateMainButton();
        if (mainBox.getScene() != null && mainBox.getScene().getWindow() != null)
            mainBox.getScene().getWindow().sizeToScene();
    }

    private List<Node> badgesFor(CertificateStatusChecker.Result result) {
        var badges = new ArrayList<Node>();
        if (result.status() == CertificateStatus.REVOKED)
            badges.add(SignatureBadgeFactory.createInvalidBadge(i18n("pickKey.certificateStatus.revoked")));
        else if (result.status() != CertificateStatus.GOOD)
            badges.add(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.certificateStatus.unknown")));

        if (result.qualification() != Qualification.UNKNOWN && !isTypeAllowed(result.type()))
            badges.add(SignatureBadgeFactory.createInvalidBadge(i18n("pickKey.type.notAllowed")));
        else if (result.qualification() == Qualification.NOT_QUALIFIED)
            badges.add(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.qualification.notQualified")));
        else if (result.qualification() == Qualification.UNKNOWN)
            badges.add(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.qualification.unknown")));

        return badges;
    }

    /** Required types replace the setting for non-qualified certificates, the website or application asked for them. */
    private boolean isAllowed(CertificateStatusChecker.Result result) {
        if (result.status() == CertificateStatus.REVOKED)
            return false;

        if (!allowedTypes.isEmpty())
            return isTypeAllowed(result.type());

        return result.qualification() != Qualification.NOT_QUALIFIED || userSettings.isNonQualifiedCertsEnabled();
    }

    private boolean isTypeAllowed(SigningCertificateType type) {
        return allowedTypes.isEmpty() || type != null && allowedTypes.contains(type);
    }

    private void showHintIfNothingFits() {
        if (results.isEmpty())
            return;

        String text;
        if (!allowedTypes.isEmpty()) {
            if (results.values().stream().anyMatch(r -> isTypeAllowed(r.type())))
                return;

            var types = allowedTypes.stream().sorted().map(type -> i18n("signingCertificateType." + type.name())).toList();
            text = i18n("pickKey.hint.typeRequired", String.join(", ", types));
            var countries = countriesTurnedOff();
            if (!countries.isEmpty())
                text += " " + i18n("pickKey.hint.countryTurnedOff", String.join(", ", countries));
        } else {
            if (!results.values().stream().allMatch(r -> r.qualification() == Qualification.NOT_QUALIFIED))
                return;

            var countries = countriesTurnedOff();
            text = countries.isEmpty()
                    ? i18n("pickKey.hint.notQualified")
                    : i18n("pickKey.hint.countryTurnedOff", String.join(", ", countries));
            if (!userSettings.isNonQualifiedCertsEnabled())
                text += " " + i18n("pickKey.hint.allowNotQualified");
        }

        hintText.setText(text);
        hint.setVisible(true);
        hint.setManaged(true);
    }

    private List<String> countriesTurnedOff() {
        var locale = resources.getLocale();
        return results.keySet().stream()
                .map(key -> CertificateStatusChecker.issuerCountry(key.getCertificate()))
                .flatMap(Optional::stream)
                .map(Country::withTrustedList)
                .flatMap(Optional::stream)
                .filter(country -> !userSettings.getTrustedList().contains(country.getShortname()))
                .map(country -> country.getName(locale))
                .distinct()
                .toList();
    }

    /** Tells the user why the checks take longer, e.g. right after Autogram started. */
    public void setTrustedListsLoading(boolean trustedListsLoading) {
        this.trustedListsLoading = trustedListsLoading;
        updateMainButton();
    }

    private void updateMainButton() {
        var checking = !checkPending.isEmpty();
        var nothingToPick = toggleGroup.getToggles().stream().allMatch(toggle -> ((RadioButton) toggle).isDisabled());
        mainButton.setDisable(checking || nothingToPick);
        if (!checking)
            mainButton.setText(mainButtonText);
        else if (trustedListsLoading)
            mainButton.setText(i18n("pickKey.trustedListsLoading.btn"));
        else
            mainButton.setText(i18n("pickKey.certificateStatus.checking.btn"));
    }

    public void onOpenSettingsAction(ActionEvent actionEvent) {
        GUIUtils.closeWindow(mainBox);
        openSettings.run();
    }

    public void onPickCertificateButtonAction(ActionEvent actionEvent) {
        if (toggleGroup.getSelectedToggle() == null) {
            error.setManaged(true);
            formGroup.getStyleClass().add("autogram-form-group--error");
            formGroup.getScene().getWindow().sizeToScene();
        } else {
            GUIUtils.closeWindow(mainBox);
            var key = (DSSPrivateKeyEntry) toggleGroup.getSelectedToggle().getUserData();
            var result = results.get(key);
            callback.accept(key, result != null ? result.type() : null);
        }
    }
}
