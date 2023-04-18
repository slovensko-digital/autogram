package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.Autogram;
import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.core.errors.AutogramException;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import digital.slovensko.autogram.util.DSSUtils;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class CliUI implements UI {
    SigningKey activeKey;

    @Override
    public void startSigning(SigningJob job, Autogram autogram) {
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
    public void pickTokenDriverAndThen(List<TokenDriver> drivers, TokenDriverLambda callback) {
        TokenDriver pickedDriver;
        if (drivers.size() == 1) {
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
        callback.call(pickedDriver);
    }

    @Override
    public void requestPasswordAndThen(TokenDriver driver, PasswordLambda callback) {
        if (!driver.needsPassword()) {
            callback.call(null);
            return;
        }
        System.out.println("Zadajte bezpecnostny kod k ulozisku certifikatov: ");
        callback.call(CliUtils.readLine()); // TODO do not show pin
    }

    @Override
    public void pickKeyAndThen(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        if(keys.size() > 1) {
            System.out.println("Found multiple keys:");
            keys.forEach(key -> System.out.println(DSSUtils.buildTooltipLabel(key)));
        }

        System.out.println("Picking key: " + DSSUtils.buildTooltipLabel(keys.get(0)));
        callback.call(keys.get(0));
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
        System.err.println(e);
    }

    @Override
    public void onPickSigningKeyFailed(AutogramException e) {
        System.err.println(e);
    }
}
