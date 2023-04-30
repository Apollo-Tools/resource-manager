package at.uibk.dps.rm.util.misc;

import lombok.experimental.UtilityClass;

/**
 * This class is used to format console output.
 *
 * @author matthi-g
 */
@UtilityClass
public class ConsoleOutputUtility {

    /**
     * Escape formatted consoleOutput.
     *
     * @param consoleOutput the consoleOutput
     * @return the escaped console output
     */
    public static String escapeConsoleOutput(String consoleOutput) {
        // Source for regex: https://regex101.com/r/96ZckU/1
        return consoleOutput.replaceAll("\\x1B(?:[@-Z\\-_]|\\[[0-?]*[ -/]*[@-~])", "");
    }
}
