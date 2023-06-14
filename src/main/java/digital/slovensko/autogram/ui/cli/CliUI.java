package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.core.errors.NoDriversDetectedException;
import digital.slovensko.autogram.core.errors.NoKeysDetectedException;
import digital.slovensko.autogram.core.errors.PDFAComplianceException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CliUI implements UI {
    SigningKey activeKey;

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
        System.out.println("Starting signing for " + job);
        if (activeKey == null) {
            autogram.pickSigningKeyAndThen(key -> {
                activeKey = key;
                autogram.sign(job, activeKey);
            });
        } else {
            autogram.sign(job, activeKey);
        }

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
            System.out.println("Vyberte ulozisko certifikatov");
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
        System.out.println("Zadajte bezpecnostny kod k ulozisku certifikatov: ");
        callback.accept(CliUtils.readLine()); // TODO do not show pin
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, Consumer<DSSPrivateKeyEntry> callback) {
        if (keys.isEmpty()) {
            showError(new NoKeysDetectedException());
            return;
        } else if (keys.size() > 1) {
            System.out.println("Found multiple keys:");
            keys.forEach(key -> System.out.println(DSSUtils.buildTooltipLabel(key)));
        }

        System.out.println("Picking key: " + DSSUtils.buildTooltipLabel(keys.get(0)));
        callback.accept(keys.get(0));
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
        System.out.println("Success for " + job);
    }

    @Override
    public void onSigningFailed(AutogramException e) {
        throw e;
    }

    @Override
    public void onDocumentSaved(File filename) {
        System.out.println("Dokument bol úspešne podpísaný");
        System.out.println("");
        System.out.println(String.format("Podpísaný súbor je uložený ako %s v priečinku %s.", filename.getName(), filename.getParent()));
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        showError(e);
    }

    @Override
    public void onUpdateAvailable() {
        System.out.println("Nová verzia");
        System.out.println(String.format("Je dostupná nová verzia a odporúčame stiahnuť aktualizáciu. Najnovšiu verziu si možete vždy stiahnuť na %s.", Updater.LATEST_RELEASE_URL));
    }

    @Override
    public void onAboutInfo() {
        System.out.println("""
        O projekte Autogram
        Autogram je jednoduchý nástroj na podpisovanie podľa európskej regulácie eIDAS, slovenských zákonov a štandardov. Môžete ho používať komerčne aj nekomerčne a úplne zadarmo.
        Autori a sponzori
        Autormi tohto projektu sú Jakub Ďuraš, Slovensko.Digital, CRYSTAL CONSULTING, s.r.o, Solver IT s.r.o. a ďalší spoluautori.
        Licencia a zdrojové kódy
        Tento softvér pôvodne vychádza projektu z Octosign White Label od Jakuba Ďuraša, ktorý je licencovaný pod MIT licenciou. So súhlasom autora je táto verzia distribuovaná pod licenciou EUPL v1.2.
        Zdrojové kódy sú dostupné na https://github.com/slovensko-digital/autogram.""");
        System.out.println(String.format("Verzia: %s", Main.getVersion()));
    }

    @Override
    public void onPDFAComplianceCheckFailed(SigningJob job) {
        throw new PDFAComplianceException();
    }

    public void showError(AutogramException e) {
        System.err.println(e.getHeading());
        System.err.println("");
        System.err.println(e.getSubheading());
        System.err.println("");
        System.err.println(e.getDescription());
    }
}
