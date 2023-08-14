package digital.slovensko.autogram.core;

import java.util.Arrays;
import java.util.List;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

public class UserSettings {

    private enum UserSettingsKeys {
        DRIVER("driver"),
        SERVER_ENABLED("server_enabled"),
        PDFA_COMPLIANCE("pdfa_compliance"),
        TRUSTED_LIST("trusted_list"),
        EN319132("en319132"),
        SIGN_FOLDER("sign_folder"),
        SIGN_AS_ASICE("sign_as_asice"),
        SIGNATURE_FORM("signature_form");

        private String key;

        UserSettingsKeys(String key) {
            this.key = key;
        }

        public String getKey() {
            return key;
        }
    }

    // TODO: default values and return types
    private static Preferences prefs = Preferences.userNodeForPackage(UserSettings.class);

    public static String getDriver() {
        return prefs.get(UserSettingsKeys.DRIVER.getKey(), null);
    }

    public static void setDriver(String driver) {
        prefs.put(UserSettingsKeys.DRIVER.getKey(), driver);
    }

    public static boolean isServerEnabled() {
        return prefs.getBoolean(UserSettingsKeys.SERVER_ENABLED.getKey(), true);
    }

    public static void setServerEnabled(boolean enabled) {
        prefs.putBoolean(UserSettingsKeys.SERVER_ENABLED.getKey(), enabled);
    }

    public static boolean shouldCheckPDFACompliance() {
        return prefs.getBoolean(UserSettingsKeys.PDFA_COMPLIANCE.getKey(), true);
    }

    public static void setShouldCheckPDFACompliance(boolean shouldCheckPDFACompliance) {
        prefs.putBoolean(UserSettingsKeys.PDFA_COMPLIANCE.getKey(), shouldCheckPDFACompliance);
    }

    public static List<String> getTrustedList() {
        return Arrays.asList(prefs.get(UserSettingsKeys.TRUSTED_LIST.getKey(), "SK,CZ,AT,HU,PL"));
    }

    public static void setTrustedList(List<String> trustedList) {
        prefs.put(UserSettingsKeys.TRUSTED_LIST.getKey(), trustedList.stream().collect(Collectors.joining()));
    }

    public static boolean isEn319132() {
        return prefs.getBoolean(UserSettingsKeys.EN319132.getKey(), false);
    }

    public static void setEn319132(boolean en319132) {
        prefs.putBoolean(UserSettingsKeys.EN319132.getKey(), en319132);
    }

    public static boolean shouldSignFolder() {
        return prefs.getBoolean(UserSettingsKeys.SIGN_FOLDER.getKey(), true);
    }

    public static void setShouldSignFolder(boolean shouldSignFolder) {
        prefs.putBoolean(UserSettingsKeys.SIGN_FOLDER.getKey(), shouldSignFolder);
    }

    public static boolean shouldSignAsAsice() {
        return prefs.getBoolean(UserSettingsKeys.SIGN_AS_ASICE.getKey(), true);
    }

    public static void setShouldSignAsAsice(boolean shouldSignAsAsice) {
        prefs.putBoolean(UserSettingsKeys.SIGN_AS_ASICE.getKey(), shouldSignAsAsice);
    }

    public static String getSignatureForm() {
        return prefs.get(UserSettingsKeys.SIGNATURE_FORM.getKey(), null);
    }

    public static void setSignatureForm(String signatureForm) {
        prefs.put(UserSettingsKeys.SIGNATURE_FORM.getKey(), signatureForm);
    }
}
