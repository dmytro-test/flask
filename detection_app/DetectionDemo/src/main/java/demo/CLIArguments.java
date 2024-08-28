package demo;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class CLIArguments {

    private final static Logger LOGGER = Logger.getLogger(CLIArguments.class.getName());

    private static final CLIArguments INSTANCE = new CLIArguments();

    private static final String PROGRAM_NAME = "DetectionDemo";

    private static final String DEFAULT_SOURCE_LOCATION = System.getProperty("user.home") + File.separator
            + "Work" + File.separator
            + "soot" + File.separator
            + "HelloSoot";

    private static final String DEFAULT_JAR_LOCATION = System.getProperty("user.home") + File.separator
            + "Work" + File.separator
            + "soot" + File.separator
            + "HelloSoot";

    private static final String TAMPERED_JAR_LOCATION = System.getProperty("user.home") + File.separator
            + "Work" + File.separator
            + "soot" + File.separator
            + "HelloSootTampered";

    private static final String OUTPUT_GRAPH_PATH = System.getProperty("user.home") + File.separator
            + "Work" + File.separator
            + "TamperedGraph.json";

    private static final String DEFAULT_CLASS_NAME = "FizzBuzz";
    private static final String DEFAULT_METHOD_NAME = "fizzBuzz";

    private Path sourceCodePath;
    private Path artifactPath;
    private Path tamperedPath;
    private Path outputJsonPath;
    private String className;
    private String methodName;
    private String buildId;
    private boolean diffOnly;
    private boolean toDraw;

    public static CLIArguments getInstance() {
        return INSTANCE;
    }

    public void parseArguments(String[] args) throws Exception {
        LOGGER.info("Parse arguments started");

        Namespace namespace = configureArgumentsAndGetNamespace(args);

        sourceCodePath = parsePathArgument("source", namespace, true);
        artifactPath = parsePathArgument("artifact", namespace, true);
        tamperedPath = parsePathArgument("tampered", namespace, true);
        outputJsonPath = parsePathArgument("output", namespace, false);
        className = parseClassName(namespace);
        methodName = parseMethodName(namespace);
        buildId = parseBuildId(namespace);
        toDraw = parseToDraw(namespace);
        diffOnly = parseDiffOnly(namespace);

        LOGGER.info("Argument paths parsed successfully");
        LOGGER.info(toString());
    }

    private Namespace configureArgumentsAndGetNamespace(String[] args) throws ArgumentParserException {
        ArgumentParser argumentParser = ArgumentParsers.newFor(PROGRAM_NAME).build()
                .defaultHelp(true)
                .description("Run verification");

        argumentParser.addArgument("-s", "--source")
                .setDefault(DEFAULT_SOURCE_LOCATION)
                .help("Specify source code path");

        argumentParser.addArgument("-a", "--artifact")
                .setDefault(DEFAULT_JAR_LOCATION)
                .help("Specify artifact path");

        argumentParser.addArgument("-t", "--tampered")
                .setDefault(TAMPERED_JAR_LOCATION)
                .help("Specify tampered artifact path");

        argumentParser.addArgument("-c", "--clazz")
                .setDefault(DEFAULT_CLASS_NAME)
                .help("Specify class name of entry point");

        argumentParser.addArgument("-m", "--method")
                .setDefault(DEFAULT_METHOD_NAME)
                .help("Specify method signature of entry point. Example: void connect(java.lang.String)");

        argumentParser.addArgument("-d", "--draw")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Specify if required to draw the output graph");

        argumentParser.addArgument("--diff")
                .action(Arguments.storeTrue())
                .setDefault(false)
                .help("Specify if diff only is required for output");

        argumentParser.addArgument("-o", "--output")
                .setDefault(OUTPUT_GRAPH_PATH)
                .help("Specify path for output graph in json format");

        argumentParser.addArgument("-b", "--build")
                .help("Specify build id");

        return argumentParser.parseArgs(args);
    }

    private String parseBuildId(Namespace namespace) {
        return namespace.getString("build");
    }

    private boolean parseToDraw(Namespace namespace) {
        return namespace.getBoolean("draw");
    }

    private boolean parseDiffOnly(Namespace namespace) {
        return namespace.getBoolean("diff");
    }

    private String parseMethodName(Namespace namespace) {
        String methodNameArgument = namespace.getString("method");
        if (methodNameArgument == null) {
            LOGGER.severe("Provided Entry point method name is not valid");
            System.exit(1);
        }
        return methodNameArgument;
    }

    private String parseClassName(Namespace namespace) {
        String classNameArgument = namespace.getString("clazz");
        if (classNameArgument == null) {
            LOGGER.severe("Provided Entry point class name is not valid");
            System.exit(1);
        }
        return classNameArgument;
    }

    private Path parsePathArgument(String argumentName, Namespace namespace, boolean isRequired) throws Exception {
        String pathArgument = namespace.getString(argumentName);
        if (pathArgument == null) {
            final String errorMessage = String.format("Path for [%s] hasn't been provided", argumentName);
            LOGGER.severe(errorMessage);
            throw new Exception(errorMessage);
        }

        Path path = Paths.get(pathArgument);

        if (isRequired && !path.toFile().exists()) {
            final String errorMessage = String.format("Provided %s path [%s] doesn't exist", argumentName, pathArgument);
            LOGGER.severe(errorMessage);
            throw new Exception(errorMessage);
        }
        return path;
    }

    public Path getSourceCodePath() {
        return sourceCodePath;
    }

    public Path getArtifactPath() {
        return artifactPath;
    }

    public Path getTamperedPath() { return tamperedPath; }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public Path getOutputJsonPath() { return outputJsonPath; }

    public String getBuildId() { return buildId; }

    public boolean isToDraw() {
        return toDraw;
    }

    public boolean isDiffOnly() { return diffOnly; }

    @Override
    public String toString() {
        return "CLIArguments{" +
                "sourceCodePath=" + sourceCodePath +
                ", artifactPath=" + artifactPath +
                ", tamperedPath=" + tamperedPath +
                ", outputJsonPath=" + outputJsonPath +
                ", className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", buildId='" + buildId + '\'' +
                ", diffOnly=" + diffOnly +
                ", toDraw=" + toDraw +
                '}';
    }
}


