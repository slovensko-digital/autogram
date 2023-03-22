package digital.slovensko.autogram.ui.cli;

import digital.slovensko.autogram.core.*;
import digital.slovensko.autogram.drivers.TokenDriver;
import digital.slovensko.autogram.ui.UI;
import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public class CliUI implements UI {
    @Override
    public void start(Autogram autogram, String[] args) {
        System.out.println("Starting CLI with args " + args.toString());
    }

    @Override
    public void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback) {
        System.out.println("Found " + keys.size() + " keys, picking first!");
        callback.call(keys.get(0));
    }

    @Override
    public void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback) {
        System.out.println("Found " + drivers.size() + " drivers, picking first!");
        callback.call(drivers.get(0));
    }

    @Override
    public void showSigningDialog(SigningJob job, Autogram autogram) {
        System.out.println("Dialog for signing " + job.getDocument().toString() + " started!");
        System.out.println("Assuming user clicked to sign it!");
        autogram.sign(job);
    }

    @Override
    public void hideSigningDialog(SigningJob job, Autogram autogram) {
        System.out.println("Dialog for signing " + job.getDocument().toString() + " closed!");
    }

    @Override
    public void refreshSigningKey() {
        System.out.println("Showing new signing key on all dialogs!");
    }
}
