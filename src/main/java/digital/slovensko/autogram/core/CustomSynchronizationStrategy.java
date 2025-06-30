package digital.slovensko.autogram.core;

import eu.europa.esig.dss.model.tsl.TLInfo;
import eu.europa.esig.dss.tsl.sync.ExpirationAndSignatureCheckStrategy;

import java.util.Date;

public class CustomSynchronizationStrategy extends ExpirationAndSignatureCheckStrategy {
	@Override
	public boolean canBeSynchronized(TLInfo trustedList) {
        var signingCertificate = trustedList.getValidationCacheInfo().getSigningCertificate();

        if (signingCertificate == null)
            return false;

        if (!signingCertificate.isValidOn(new Date()))
            return false;

        return super.canBeSynchronized(trustedList);
	}
}
