package digital.slovensko.autogram.core;

import static java.nio.charset.StandardCharsets.UTF_16LE;

import java.io.IOException;
import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.SymbolLookup;
import java.lang.foreign.ValueLayout;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import digital.slovensko.autogram.util.Logging;
import digital.slovensko.autogram.util.MsixPackage;

public record LegacyInstallation(String productCode, String version) {
    private static final List<String> UPGRADE_CODES = Stream.concat(
                    Stream.of(Configuration.getBuildProperty("win.upgradeUUID")),
                    Arrays.stream(Configuration.getBuildProperty("win.previousUpgradeUUIDs").split(",")))
            .map(String::strip)
            .filter(uuid -> !uuid.isEmpty())
            .map(uuid -> "{" + uuid + "}")
            .toList();
    private static final int ERROR_SUCCESS = 0;
    private static final int ERROR_UNKNOWN_PRODUCT = 1605;
    private static final int ERROR_SUCCESS_REBOOT_REQUIRED = 3010;
    private static final int PRODUCT_CODE_LENGTH = 39;
    private static final int VERSION_LENGTH = 64;

    public static List<LegacyInstallation> findWhenRunningFromMsix() {
        if (!MsixPackage.isCurrent())
            return List.of();

        try {
            return find();
        } catch (Throwable e) {
            Logging.log("Unable to find MSI installations of Autogram: " + e);
            return List.of();
        }
    }

    private static List<LegacyInstallation> find() throws Throwable {
        var installations = new ArrayList<LegacyInstallation>();
        try (var arena = Arena.ofConfined()) {
            var linker = Linker.nativeLinker();
            var msi = SymbolLookup.libraryLookup("msi", arena);
            var enumRelatedProducts = linker.downcallHandle(msi.findOrThrow("MsiEnumRelatedProductsW"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.JAVA_INT,
                            ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
            var getProductInfo = linker.downcallHandle(msi.findOrThrow("MsiGetProductInfoW"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                            ValueLayout.ADDRESS, ValueLayout.ADDRESS));

            var versionAttribute = arena.allocateFrom("VersionString", UTF_16LE);
            for (var upgradeCode : UPGRADE_CODES) {
                var upgradeCodeString = arena.allocateFrom(upgradeCode, UTF_16LE);
                for (int i = 0; ; i++) {
                    var productCode = arena.allocate(ValueLayout.JAVA_CHAR, PRODUCT_CODE_LENGTH);
                    if ((int) enumRelatedProducts.invokeExact(upgradeCodeString, 0, i, productCode) != ERROR_SUCCESS)
                        break;

                    var version = arena.allocate(ValueLayout.JAVA_CHAR, VERSION_LENGTH);
                    var versionLength = arena.allocate(ValueLayout.JAVA_INT);
                    versionLength.set(ValueLayout.JAVA_INT, 0, VERSION_LENGTH);
                    var versionFound = (int) getProductInfo.invokeExact(productCode, versionAttribute, version,
                            versionLength) == ERROR_SUCCESS;

                    installations.add(new LegacyInstallation(productCode.getString(0, UTF_16LE),
                            versionFound ? version.getString(0, UTF_16LE) : ""));
                }
            }
        }

        Logging.log("MSI installations of Autogram: " + installations);
        return installations;
    }

    public int uninstall() throws IOException, InterruptedException {
        // Without MSIX_MIGRATION the MSI package would terminate this app as well
        var process = new ProcessBuilder("msiexec.exe", "/x", productCode, "/passive", "/norestart", "MSIX_MIGRATION=1")
                .redirectOutput(ProcessBuilder.Redirect.DISCARD)
                .redirectError(ProcessBuilder.Redirect.DISCARD)
                .start();
        var exitCode = process.waitFor();
        Logging.log("Uninstalling MSI installation " + this + " ended with " + exitCode);
        return exitCode;
    }

    public static boolean isUninstalled(int msiexecExitCode) {
        return msiexecExitCode == ERROR_SUCCESS || msiexecExitCode == ERROR_SUCCESS_REBOOT_REQUIRED
                || msiexecExitCode == ERROR_UNKNOWN_PRODUCT;
    }
}
