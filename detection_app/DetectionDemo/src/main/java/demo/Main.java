package demo;

import demo.check.DetectionEngine;

import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {

    private final static Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static final int ERROR = 1;

    public static void main(String[] args) {
        LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.ALL);
        LOGGER.info("Detection run started");

        try {
            CLIArguments.getInstance().parseArguments(args);
            DetectionEngine.run();
        } catch (Exception e) {
            LOGGER.severe("Failed to execute Detection Demo");
            LOGGER.throwing("", "", e);
            System.exit(ERROR);
        }
    }
}
