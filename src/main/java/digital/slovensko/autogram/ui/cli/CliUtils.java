package digital.slovensko.autogram.ui.cli;

import java.util.Scanner;

public class CliUtils {
    static Scanner scanner = new Scanner(System.in);

    public static char[] readLine() {
        return scanner.nextLine().toCharArray();
    }

    public static int readInteger() {
        var i = scanner.nextInt();
        scanner.nextLine(); // consume newline
        return i;
    }
}
