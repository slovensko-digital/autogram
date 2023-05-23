package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;

import java.util.function.Predicate;

public class TokenDriverPredicate implements Predicate<TokenDriver> {

    private final String driver;

    public TokenDriverPredicate(String driver) {
        this.driver = driver;
    }

    @Override
    public boolean test(TokenDriver tokenDriver) {
        return tokenDriver.getShortname().toLowerCase().equals(driver.toLowerCase());
    }
}
