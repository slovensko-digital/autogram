package digital.slovensko.autogram.util;

import digital.slovensko.autogram.Main;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Restarts the application by spawning a new JVM with the same JVM arguments,
 * classpath and program arguments as the current one, then exiting.
 */
public class ApplicationRestarter {
    public static void restart() throws IOException {
        new ProcessBuilder(buildRestartCommand()).inheritIO().start();
        Platform.exit();
        System.exit(0);
    }

    static List<String> buildRestartCommand() {
        return buildRestartCommand(
            System.getProperty("java.home") + File.separator + "bin" + File.separator + "java",
            ManagementFactory.getRuntimeMXBean().getInputArguments(),
            System.getProperty("java.class.path"),
            Main.class.getName(),
            Main.getArgs());
    }

    static List<String> buildRestartCommand(String javaBinary, List<String> jvmArgs, String classpath,
            String mainClass, String[] appArgs) {
        var command = new ArrayList<String>();
        command.add(javaBinary);
        for (var i = 0; i < jvmArgs.size(); i++) {
            var arg = jvmArgs.get(i);
            // the classpath is set explicitly below; a stale -cp from the
            // launcher would conflict with it
            if (arg.equals("-cp") || arg.equals("-classpath")) {
                i++;
                continue;
            }
            if (arg.startsWith("-cp=") || arg.startsWith("-classpath="))
                continue;
            command.add(arg);
        }
        command.add("-cp");
        command.add(classpath);
        command.add(mainClass);
        command.addAll(Arrays.asList(appArgs));
        return command;
    }
}
