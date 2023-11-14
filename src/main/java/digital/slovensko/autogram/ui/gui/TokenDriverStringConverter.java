package digital.slovensko.autogram.ui.gui;

import digital.slovensko.autogram.core.DefaultDriverDetector;
import digital.slovensko.autogram.core.DriverDetector;
import digital.slovensko.autogram.drivers.TokenDriver;
import javafx.util.StringConverter;

public class TokenDriverStringConverter extends StringConverter<TokenDriver> {
    private final DriverDetector driverDetector;
    public TokenDriverStringConverter(DefaultDriverDetector driverDetector) {
        this.driverDetector = driverDetector;
    }

    @Override
    public String toString(TokenDriver driver) {
        if (driver == null) {
            return "";
        }
        return driver.getName();
    }

    @Override
    public TokenDriver fromString(String driverName) {
        var tokenDriver = driverDetector
                .getAvailableDrivers()
                .stream()
                .filter(d -> d.getName().equals(driverName))
                .findFirst();
        return tokenDriver.isEmpty() ? null : tokenDriver.get();
    }
}
