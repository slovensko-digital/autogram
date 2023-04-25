package digital.slovensko.autogram;

import digital.slovensko.autogram.ui.gui.GUI;

import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        System.out.println("Starting with args: " + Arrays.toString(args));
        var ui = new GUI();

        ui.start(args);
    }
}
