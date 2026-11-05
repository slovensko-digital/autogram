package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.CertificateStatusChecker;
import digital.slovensko.autogram.core.CertificateStatusChecker.Qualification;
import digital.slovensko.autogram.core.UserSettings;
import digital.slovensko.autogram.core.settings.Country;
import eu.europa.esig.dss.enumerations.CertificateStatus;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static digital.slovensko.autogram.util.DSSUtils.buildTooltipLabel;
import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class PickKeyDialogController extends BaseController {
    private final Consumer<DSSPrivateKeyEntry> callback;
    private final List<DSSPrivateKeyEntry> keys;
    private final UserSettings userSettings;
    private final BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate;
    private final Runnable openSettings;
    private final Function<List<Country>, CompletableFuture<?>> turnOnTrustedLists;
    private final Set<DSSPrivateKeyEntry> checkPending = new HashSet<>();
    private final Map<DSSPrivateKeyEntry, CertificateStatusChecker.Result> results = new LinkedHashMap<>();
    private final Map<DSSPrivateKeyEntry, Runnable> checks = new LinkedHashMap<>();

    @FXML
    VBox formGroup;
    @FXML
    Text error;
    @FXML
    VBox mainBox;
    @FXML
    VBox radios;
    @FXML
    TextFlow notVerifiedHint;
    @FXML
    Text notVerifiedText;
    @FXML
    VBox countriesTurnedOffWarning;
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
     * @param openSettings opens the settings after this dialog closed itself, null when the settings are not available
     * @param turnOnTrustedLists adds the countries in the settings and completes once their trusted lists are loaded,
     *                           null when the settings are not available
     */
    public PickKeyDialogController(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback, UserSettings userSettings,
            BiConsumer<DSSPrivateKeyEntry, Consumer<CertificateStatusChecker.Result>> checkCertificate, Runnable openSettings,
            Function<List<Country>, CompletableFuture<?>> turnOnTrustedLists) {
        this.keys = keys;
        this.callback = callback;
        this.userSettings = userSettings;
        this.checkCertificate = checkCertificate;
        this.openSettings = openSettings;
        this.turnOnTrustedLists = turnOnTrustedLists;
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
                badges.getChildren().add(SignatureBadgeFactory.createInfoBadge(i18n("pickKey.certificateStatus.expired")));

                if (!userSettings.isExpiredCertsEnabled())
                    continue;
            }

            var radioButton = new RadioButton(parseCN(key.getCertificate().getSubject().getRFC2253()));
            radioButton.setToggleGroup(toggleGroup);
            radioButton.setUserData(key);
            // a long name wraps rather than being cut off or squeezing the badge
            radioButton.setWrapText(true);
            radioButton.setMinHeight(Region.USE_PREF_SIZE);
            badges.setMinWidth(Region.USE_PREF_SIZE);
            radios.getChildren().add(new HBox(radioButton, badges));

            var tooltip = new Tooltip(buildTooltipLabel(key));
            tooltip.setShowDuration(Duration.seconds(10));
            tooltip.setWrapText(true);
            tooltip.setPrefWidth(400);
            radioButton.setTooltip(tooltip);

            if (!expired)
                checks.put(key, () -> {
                    radioButton.setDisable(false);
                    badges.getChildren().clear();
                    checkCertificate.accept(key, result -> showResult(key, radioButton, badges, result));
                });
        }

        selectTheOnlyCertificate();
        settingsLink.setVisible(openSettings != null);
        settingsLink.setManaged(openSettings != null);
        checkAll();
        updateMainButton();
    }

    private void selectTheOnlyCertificate() {
        if (keys.size() == 1)
            toggleGroup.getToggles().get(0).setSelected(true);
    }

    private void checkAll() {
        checkPending.addAll(checks.keySet());
        checks.values().forEach(Runnable::run);
    }

    private void showResult(DSSPrivateKeyEntry key, RadioButton radioButton, HBox badges, CertificateStatusChecker.Result result) {
        checkPending.remove(key);
        results.put(key, result);
        badges.getChildren().setAll(badgeFor(result).stream().toList());

        if (!isAllowed(result)) {
            radioButton.setSelected(false);
            radioButton.setDisable(true);
        }

        if (checkPending.isEmpty()) {
            showHintIfNotVerified();
            showCountriesTurnedOff();
            showHintIfNothingQualified();
        }

        updateMainButton();
        sizeWindowToScene();
    }

    private void sizeWindowToScene() {
        if (mainBox.getScene() != null && mainBox.getScene().getWindow() != null)
            mainBox.getScene().getWindow().sizeToScene();
    }

    /** One badge at most, for the most important finding. */
    private Optional<Node> badgeFor(CertificateStatusChecker.Result result) {
        if (result.status() == CertificateStatus.REVOKED)
            return Optional.of(SignatureBadgeFactory.createInvalidBadge(i18n("pickKey.certificateStatus.revoked")));

        if (result.qualification() == Qualification.NOT_QUALIFIED)
            return Optional.of(SignatureBadgeFactory.createWarningBadge(i18n("pickKey.qualification.notQualified")));

        if (isNotVerified(result))
            return Optional.of(SignatureBadgeFactory.createWarningBadge(i18n(result.status() == CertificateStatus.GOOD
                    ? "pickKey.qualification.unknown" : "pickKey.certificateStatus.unknown")));

        return Optional.empty();
    }

    /** What the checks could not find out does not block signing, e.g. offline. */
    private boolean isAllowed(CertificateStatusChecker.Result result) {
        return (result.status() != CertificateStatus.REVOKED || userSettings.isExpiredCertsEnabled())
                && (result.qualification() != Qualification.NOT_QUALIFIED || userSettings.isNonQualifiedCertsEnabled());
    }

    /** Neither revoked nor not qualified, but a check could not be done, e.g. offline. Never blocks signing. */
    private static boolean isNotVerified(CertificateStatusChecker.Result result) {
        return result.status() != CertificateStatus.REVOKED && result.qualification() != Qualification.NOT_QUALIFIED
                && (result.status() != CertificateStatus.GOOD || result.qualification() == Qualification.UNKNOWN);
    }

    private void showHintIfNotVerified() {
        if (results.values().stream().noneMatch(result -> isNotVerified(result) && !isExplainedByCountryTurnedOff(result)))
            return;

        notVerifiedText.setText(i18n("pickKey.hint.notVerified"));
        setShown(notVerifiedHint, true);
    }

    /** Only the qualification is not known, which the warning about the country says. */
    private boolean isExplainedByCountryTurnedOff(CertificateStatusChecker.Result result) {
        return turnOnTrustedLists != null && result.countryToTurnOn() != null && result.status() == CertificateStatus.GOOD;
    }

    private void showCountriesTurnedOff() {
        var countryPerCertificate = results.values().stream()
                .map(CertificateStatusChecker.Result::countryToTurnOn)
                .filter(Objects::nonNull)
                .toList();
        if (turnOnTrustedLists == null || countryPerCertificate.isEmpty())
            return;

        var countries = countryPerCertificate.stream().distinct().toList();
        var message = i18n("pickKey.countryTurnedOff.warning", GUIValidationUtils.countryNames(resources, countries),
                countries.size(), countryPerCertificate.size());
        countriesTurnedOffWarning.getChildren().setAll(GUIValidationUtils.createCountriesTurnedOffWarning(resources,
                message, countries, this::addCountries));
        setShown(countriesTurnedOffWarning, true);
    }

    /** Checks the certificates again with the trusted lists of the countries. */
    private void addCountries(List<Country> countries) {
        mainBox.requestFocus(); // the button disappears and its focus would move to the sign button
        waitForTrustedLists(turnOnTrustedLists.apply(countries));
        results.clear();
        setShown(notVerifiedHint, false);
        setShown(countriesTurnedOffWarning, false);
        setShown(hint, false);
        selectTheOnlyCertificate();
        checkAll();
        updateMainButton();
        sizeWindowToScene();
    }

    private void showHintIfNothingQualified() {
        if (results.isEmpty() || !results.values().stream().allMatch(r -> r.qualification() == Qualification.NOT_QUALIFIED))
            return;

        var text = i18n("pickKey.hint.notQualified");
        if (!userSettings.isNonQualifiedCertsEnabled())
            text += " " + i18n("pickKey.hint.allowNotQualified");

        hintText.setText(text);
        setShown(hint, true);
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    /** Tells the user why the checks take longer, e.g. right after Autogram started. */
    public void setTrustedListsLoading(boolean trustedListsLoading) {
        this.trustedListsLoading = trustedListsLoading;
        updateMainButton();
    }

    public void waitForTrustedLists(CompletableFuture<?> trustedLists) {
        if (trustedLists.isDone())
            return;

        setTrustedListsLoading(true);
        trustedLists.whenComplete((verifier, e) -> Platform.runLater(() -> setTrustedListsLoading(false)));
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
            callback.accept(key);
        }
    }
}
