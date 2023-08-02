package digital.slovensko.autogram.ui.cli;

import static digital.slovensko.autogram.util.DSSUtils.parseCN;

import java.io.File;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.Batch;
import digital.slovensko.autogram.core.SigningJob;
import digital.slovensko.autogram.core.SigningKey;
import digital.slovensko.autogram.core.Updater;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.FunctionCanceledException;
import digital.slovensko.autogram.core.errors.InitializationFailedException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.core.errors.PDFAComplianceException;
import digital.slovensko.autogram.core.errors.PINIncorrectException;
import digital.slovensko.autogram.core.errors.PINLockedException;
import digital.slovensko.autogram.core.errors.SigningCanceledByUserException;
import digital.slovensko.autogram.core.errors.SigningWithExpiredCertificateException;
import digital.slovensko.autogram.core.errors.SourceAndTargetTypeMismatchException;
import digital.slovensko.autogram.core.errors.SourceDoesNotExistException;
import digital.slovensko.autogram.core.errors.SourceNotDefindedException;
import digital.slovensko.autogram.core.errors.TargetAlreadyExistsException;
import digital.slovensko.autogram.core.errors.TargetDirectoryDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenDriverDoesNotExistException;
import digital.slovensko.autogram.core.errors.TokenNotRecognizedException;
import digital.slovensko.autogram.core.errors.TokenRemovedException;
import digital.slovensko.autogram.core.errors.UnableToCreateDirectoryException;
import digital.slovensko.autogram.core.visualization.Visualization;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.BatchUiResult;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.ui.gui.IgnorableException;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

public class CliUI implements UI {
    SigningKey activeKey;
    int nJobsSigned = 1;
    int nJobsTotal = 0;

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
        System.out.println("Starting signing file \"%s\" [%d/%d]".formatted(job.getDocument().getName(), nJobsSigned++,
                nJobsTotal));
        autogram.sign(job, activeKey);
    }

    public void setJobsCount(int nJobsTotal) {
        this.nJobsTotal = nJobsTotal;
    }

    @Override
    public void startBatch(Batch batch, Autogram autogram, Consumer<SigningKey> callback) {
        // TODO Auto-generated method stub
    }

    @Override
    public void signBatch(SigningJob job, SigningKey key) {
        // TODO Auto-generated method stub
    }

    @Override
    public void cancelBatch(Batch batch) {
        // TODO Auto-generated method stub
    }

    @Override
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, Consumer<TokenDriver> callback) {
        TokenDriver pickedDriver;
        if (drivers.isEmpty()) {
            showError(new NoDriversDetectedException());
            return;
        } else if (drivers.size() == 1) {
            pickedDriver = drivers.get(0);
        } else {
            var i = new AtomicInteger(1);
            System.out.println("Pick driver:");
            drivers.forEach(driver -> {
                System.out.print("[" + i + "] ");
                System.out.println(driver.getName());
                i.addAndGet(1);
            });
            pickedDriver = drivers.get(CliUtils.readInteger() - 1);
        }
        callback.accept(pickedDriver);
    }

    @Override
    public void requestPasswordAndThen(TokenDriver driver, Consumer<char[]> callback) {
        if (!driver.needsPassword()) {
            callback.accept(null);
            return;
        }

        // Read password from CLI
        var password = System.console().readPassword("Enter security code for driver (hidden): ");
        callback.accept(password);
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            return;
        }

        if (keys.size() == 1) {
            callback.accept(keys.get(0));
            return;
        }

        var i = new AtomicInteger(1);
        System.out.println("Pick Key:");
        keys.forEach(key -> {
            System.out.print("[" + i + "] ");
            System.out.println(parseCN(key.getCertificate().getSubject().getRFC2253()));
            i.addAndGet(1);
        });
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
    public void showVisualization(Visualization visualization, Autogram autogram) {

    }

    @Override
    public void showIgnorableExceptionDialog(IgnorableException exception) {
        throw exception;
    }

    @Override
    public void showError(AutogramException e) {
        String errMessage = "";
        if (e instanceof FunctionCanceledException) {
            errMessage = "No security code entered";
        } else if (e instanceof InitializationFailedException) {
            errMessage = "Unable to read card";
        } else if (e instanceof NoDriversDetectedException) {
            errMessage = "No available drivers found";
        } else if (e instanceof NoKeysDetectedException) {
            errMessage = "No signing keys found";
        } else if (e instanceof PDFAComplianceException) {
            errMessage = "Document is not PDF/A compliant";
        } else if (e instanceof PINIncorrectException) {
            errMessage = "Incorrect security code";
        } else if (e instanceof PINLockedException) {
            errMessage = "PIN is blocked";
        } else if (e instanceof SigningCanceledByUserException) {
            errMessage = "Signing canceled by user";
        } else if (e instanceof SigningWithExpiredCertificateException) {
            errMessage = "Signing with expired certificate";
        } else if (e instanceof TokenNotRecognizedException) {
            errMessage = "Token not recognized";
        } else if (e instanceof TokenRemovedException) {
            errMessage = "Token removed";
        } else if (e instanceof TargetAlreadyExistsException) {
            errMessage = "Target already exists";
        } else if (e instanceof SourceAndTargetTypeMismatchException) {
            errMessage = "Source and target type mismatch (file / directory)";
        } else if (e instanceof SourceDoesNotExistException) {
            errMessage = "Source does not exist";
        } else if (e instanceof SourceNotDefindedException) {
            errMessage = "Source not defined";
        } else if (e instanceof UnableToCreateDirectoryException) {
            errMessage = "Unable to create directory";
        } else if (e instanceof TokenDriverDoesNotExistException) {
            errMessage = "Token driver does not exist";
        } else if (e instanceof TargetDirectoryDoesNotExistException) {
            errMessage = "Target directory does not exist";
        } else {
            errMessage = "Unknown error occurred";
            e.printStackTrace();
        }
        System.err.println(errMessage);
    }
}
