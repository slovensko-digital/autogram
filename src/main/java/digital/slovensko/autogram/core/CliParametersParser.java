package digital.slovensko.autogram.core;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CliParametersParser {

    public static CliParameters parse(String[] args) {
        return init(Arrays.asList(args));
    }

    private static CliParameters init(List<String> args) {
        Map<String, String> cliParameters = new HashMap<String, String>();
        for (String arg : args) {
            if (isNamedParam(arg)) {
                final int eqIdx = arg.indexOf('=');
                String key = arg.substring(2, eqIdx);
                String value = arg.substring(eqIdx + 1);
                cliParameters.put(key, value);
            }
        }
        return new CliParameters(cliParameters);
    }

    private static boolean isNamedParam(String arg) {
        if (arg.startsWith("--")) {
            return (arg.indexOf('=') > 2 && validFirstChar(arg.charAt(2)));
        } else {
            return false;
        }
    }

    private static boolean validFirstChar(char c) {
        return Character.isLetter(c) || c == '_';
    }
}
