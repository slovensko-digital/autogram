package digital.slovensko.autogram;

import javafx.application.Application;

public class Main {
    public static void main(String[] args) {
        //Thread.setDefaultUncaughtExceptionHandler(new com.octosign.whitelabel.ui.Main.ExceptionHandler());

        SignRequestListener mode;
        // if some parameter
        // cli mode
        mode = new CliSignRequestListener(args);
        // or
        // api mode
        mode = new ApiSignRequestListener(args);

        // gui frontend or cli frontend

        FrontendMode frontend = null;
        frontend = new CliFrontendMode();
        frontend = new GuiFrontendMode();

        Autogram.start(frontend, mode, args);
    }
}
