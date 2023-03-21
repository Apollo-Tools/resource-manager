package at.uibk.dps.rm.util;

public class ConsoleOutputUtility {

    public static String escapeConsoleOutput(String consoleOutput) {
        // Source for regex: https://regex101.com/r/96ZckU/1
        return consoleOutput.replaceAll("\\x1B(?:[@-Z\\-_]|\\[[0-?]*[ -/]*[@-~])", "");
    }
}
