package digital.slovensko.autogram;

import eu.europa.esig.dss.token.DSSPrivateKeyEntry;

import java.util.List;

public interface UI {
    void pickKeyAndDo(List<DSSPrivateKeyEntry> keys, PrivateKeyLambda callback);

    void pickTokenDriverAndDo(List<TokenDriver> drivers, TokenDriverLambda callback);

    void showSigningDialog(SigningJob job, Autogram autogram);

    void refreshSigningKey(SigningKey key);
}
