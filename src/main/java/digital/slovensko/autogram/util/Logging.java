package digital.slovensko.autogram.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.application.Platform;

public class Logging {
    private static Logger logger = LoggerFactory.getLogger(Logging.class);

    public static void log(String message) {
        var date = new SimpleDateFormat("HH:mm:ss.S").format(new Date());

        logger.debug("{} ({}) {}", date, (Platform.isFxApplicationThread() ? "FX" : "BG"), message);
    }
}