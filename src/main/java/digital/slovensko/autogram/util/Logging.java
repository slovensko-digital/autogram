package digital.slovensko.autogram.util;

import java.text.SimpleDateFormat;
import java.util.Date;

import javafx.application.Platform;

public class Logging {
    public static void log(String message) {
        var date = new SimpleDateFormat("HH:mm:ss.S").format(new Date());

        System.out.println(date + (Platform.isFxApplicationThread() ? " (FX) " : " (BG) ") + " " + message);
    }
}