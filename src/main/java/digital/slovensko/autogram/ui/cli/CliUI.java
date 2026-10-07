package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.BatchStartCallback;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.Updater;
import digital.slovensko.autogram.core.ValidationReports;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.FunctionCanceledException;
import digital.slovensko.autogram.core.errors.InitializationFailedException;
import digital.slovensko.autogram.core.errors.MultipleSourcesException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.core.errors.PDFAComplianceException;
import digital.slovensko.autogram.core.errors.PDFSignatureLevelIsNotValidException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PINLockedException;
import digital.slovensko.autogram.core.errors.PkcsEidWindowsDllException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.SigningWithExpiredCertificateException;
import digital.slovensko.autogram.core.errors.SlotIndexIsNotANumberException;
import digital.slovensko.autogram.core.errors.SlotIndexOutOfRangeException;
import digital.slovensko.autogram.core.errors.SourceAndTargetTypeMismatchException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.SourceNotDefinedException;
import digital.slovensko.autogram.core.errors.TargetAlreadyExistsException;
import digital.slovensko.autogram.core.errors.TargetDirectoryDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenDriverDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenNotRecognizedException;
import digital.slovensko.autogram.core.errors.TokenRemovedException;
import digital.slovensko.autogram.core.errors.TsaServerMisconfiguredException;
import digital.slovensko.autogram.core.errors.UnableToCreateDirectoryException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.drivers.TokenOption;
import digital.slovensko.autogram.drivers.TokenOptions;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.TokenOptionDescription;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static digital.slovensko.autogram.util.DSSUtils.parseCN;

public class CliUI implements UI {
    private final CliSettings settings;
    SigningKey activeKey;
    int nJobsSigned = 1;
    int nJobsTotal = 0;

    public CliUI(CliSettings settings) {
        this.settings = settings;
    }

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        if (activeKey == null) {
            autogram.pickSigningKeyAndThen(key -> {
                activeKey = key;
                sign(job, autogram);
            });
        } else {
            sign(job, autogram);
        }

    }

    private void sign(SigningJob job, Autogram autogram) {
        System.out.println("Starting signing file \"%s\" [%d/%d]".formatted(job.getName(), nJobsSigned++,
                nJobsTotal));
        autogram.sign(job, activeKey);
    }

    public void setJobsCount(int nJobsTotal) {
        this.nJobsTotal = nJobsTotal;
    }

    @Override
    public void startBatch(Batch batch, Autogram autogram, BatchStartCallback callback) {
        // TODO Auto-generated method stub
    }

    @Override
    public void cancelBatch(Batch batch) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onTokenSearchStarted() {
    }

    @Override
    public void pickTokenAndThen(TokenOptions options, Supplier<TokenOptions> searchAgain, Consumer<TokenOption> callback, Runnable onCancel) {
        if (options.isEmpty()) {
            showError(new NoDriversDetectedException());
            return;
        }

        if (settings.getDefaultDriver() != null) {
            var defaultDriverOptions = options.forDriver(settings.getDefaultDriver());
            if (defaultDriverOptions.isEmpty())
                throw new NoDriversDetectedException();

            options = defaultDriverOptions.get();
        }

        var choices = new ArrayList<>(options.found());
        options.otherDrivers().forEach(driver -> choices.add(new TokenOption(driver, null)));
        // drivers found no card
        if (choices.isEmpty())
            throw new InitializationFailedException();

        // the user asked for the driver, even one that can't tell whether there's a card is used then, e.g. eObčanka
        if (settings.getDefaultDriver() != null && choices.size() == 1) {
            callback.accept(choices.get(0));
            return;
        }

        var automaticOption = options.getAutomaticOption();
        if (automaticOption.isPresent()) {
            callback.accept(automaticOption.get());
            return;
        }

        System.out.println("Pick card or driver:");
        var descriptions = TokenOptionDescription.describeAll(choices);
        for (int i = 0; i < choices.size(); i++) {
            var description = descriptions.get(i);
            var text = description.driverName();
            if (description.qualified() != null)
                text += description.qualified() ? ", qualified" : ", non-qualified";
            else if (description.slotNumber() != null)
                text += ", slot " + description.slotNumber();

            if (description.readerName() != null)
                text += " (reader: " + description.readerName() + ")";

            System.out.println("[" + (i + 1) + "] " + text);
        }

        callback.accept(choices.get(CliUtils.readInteger() - 1));
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, TokenDriver driver, Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException(driver.getNoKeysHelperText()));
            return;
        }

        if (keys.size() == 1) {
            callback.accept(keys.get(0));
            return;
        }

        System.out.println("Pick Key:");
        var i = 1;
        for (var key : keys) {
            var keyText = parseCN(key.getCertificate().getSubject().getRFC2253());
            if (!key.getCertificate().isValidOn(new java.util.Date()))
                keyText += " (expired certificate)";

            System.out.println("[" + i++ + "] " + keyText);
        }

        var pickedKey = keys.get(CliUtils.readInteger() - 1);
        callback.accept(pickedKey);
    }

    @Override
    public void onWorkThreadDo(Runnable callback) {
        callback.run(); // no threads
    }

    @Override
    public void onUIThreadDo(Runnable callback) {
        callback.run(); // no threads
    }

    @Override
    public void onSigningSuccess(SigningJob job) {

    }

    @Override
    public void onSigningFailed(AutogramException e, SigningJob job) {
        throw e;
    }

    @Override
    public void onSigningFailed(AutogramException e) {
        throw e;
    }

    @Override
    public void onDocumentBatchSaved(BatchUiResult result) {

    }

    @Override
    public void onDocumentSaved(File file) {
        var directory = file.getParent() != null ? " in \"%s\"".formatted(file.getParent()) : "";
        System.out
                .println("File successfully signed. Signed file saved as \"%s\"".formatted(file.getName()) + directory);
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        showError(e);
    }

    @Override
    public void onUpdateAvailable() {
        System.out.println("Nová verzia");
        System.out.println(String.format(
                "Je dostupná nová verzia a odporúčame stiahnuť aktualizáciu. Najnovšiu verziu si možete vždy stiahnuť na %s.",
                Updater.LATEST_RELEASE_URL));
    }

    @Override
    public void onAboutInfo() {
        System.out.println(
                """
                        O projekte Autogram
                        Autogram je jednoduchý nástroj na podpisovanie podľa európskeho nariadenia eIDAS, slovenských zákonov a štandardov. Môžete ho používať komerčne aj nekomerčne a úplne zadarmo.
                        Autori a sponzori
                        Autormi tohto projektu sú Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. a ďalší spoluautori.
                        Licencia a zdrojové kódy
                        Tento softvér pôvodne vychádza projektu z Octosign White Label od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou. So súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.
                        Zdrojové kódy sú dostupné na https://github.com/slovensko-digital/autogram.""");
        System.out.println(String.format("Verzia: %s", Main.getVersionString()));
    }

    @Override
    public void onPDFAComplianceCheckFailed(SigningJob job) {
        throw new PDFAComplianceException();
    }

    @Override
    public void showSigningJob(SigningJob job, Autogram autogram) {

    }

    @Override
    public void onSignatureValidationCompleted(ValidationReports reports) {

    }

    @Override
    public void onSignatureCheckCompleted(ValidationReports reports) {

    }

    public void showIgnorableExceptionDialog(IgnorableException exception) {
        throw exception;
    }

    public static String parseError(AutogramException e) {
        if (e instanceof FunctionCanceledException) {
            return "No security code entered";
        } else if (e instanceof InitializationFailedException) {
            return "Unable to read card";
        } else if (e instanceof NoDriversDetectedException) {
            return "No available drivers found";
        } else if (e instanceof NoKeysDetectedException) {
            return "No signing keys found";
        } else if (e instanceof PDFAComplianceException) {
            return "Document is not PDF/A compliant";
        } else if (e instanceof PINIncorrectException) {
            return "Incorrect security code";
        } else if (e instanceof PINLockedException) {
            return "PIN is blocked";
        } else if (e instanceof SigningCanceledByUserException) {
            return "Signing canceled by user";
        } else if (e instanceof SigningWithExpiredCertificateException) {
            return "Signing with expired certificate";
        } else if (e instanceof TokenNotRecognizedException) {
            return "Token not recognized";
        } else if (e instanceof TokenRemovedException) {
            return "Token removed";
        } else if (e instanceof TargetAlreadyExistsException) {
            return "Target already exists";
        } else if (e instanceof SourceAndTargetTypeMismatchException) {
            return "Source and target type mismatch (file / directory)";
        } else if (e instanceof SourceDoesNotExistException) {
            return "Source does not exist";
        } else if (e instanceof SourceNotDefinedException) {
            return "Source not defined";
        } else if (e instanceof MultipleSourcesException) {
            return "Only one source is allowed (use -s or a single positional argument; pass a directory to sign multiple files)";
        } else if (e instanceof UnableToCreateDirectoryException) {
            return "Unable to create directory";
        } else if (e instanceof TokenDriverDoesNotExistException) {
            return "Token driver does not exist";
        } else if (e instanceof TargetDirectoryDoesNotExistException) {
            return "Target directory does not exist";
        } else if (e instanceof SlotIndexIsNotANumberException) {
            return "Slot ID is not a number";
        } else if (e instanceof PDFSignatureLevelIsNotValidException) {
            return "PDF signature level is not valid";
        } else if (e instanceof TsaServerMisconfiguredException) {
            return "TSA server refused to add timestamp. Check TSA server configuration.";
        } else if (e instanceof SlotIndexOutOfRangeException) {
            return "Provided slot index is out of range for chosen driver.";
        } else if (e instanceof PkcsEidWindowsDllException) {
            return "PKCS library problem. Microsoft Visual C++ 2015 Redistributable probably needs to be installed.";
        } else {
            e.printStackTrace();
            return "Unknown error occurred";
        }
    }

    @Override
    public void showError(AutogramException e) {
        System.err.println(parseError(e));
    }

    @Override
    public char[] getKeystorePassword() {
        return System.console().readPassword("Enter keystore password (hidden): ");
    }

    public char[] getContextSpecificPassword() {
        return System.console().readPassword("Enter key password (hidden): ");
    }

    @Override
    public void updateBatch() {
        // TODO: no usage for this in CLI UI
    }

    @Override
    public void resetSigningKey() {
        activeKey = null;
    }

    @Override
    public void consentCertificateReadingAndThen(Consumer<Runnable> callback, Runnable onCancel) {
        System.out.println("Do you consent to reading certificates from the token? (y/n)");
        String input = System.console().readLine();
        if ("y".equalsIgnoreCase(input)) {
            callback.accept(() -> {
                System.out.println("Consent granted. Reading certificates...");
            });
        } else {
            onCancel.run();
        }
    }
}
