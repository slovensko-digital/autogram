package digital.slovensko.autogram.core;

import digital.slovensko.autogram.drivers.TokenDriver;

import java.util.List;

public interface DriverDetector {
    List<TokenDriver> getAvailableDrivers();
}
