package demo.check;

import com.google.common.graph.MutableGraph;
import demo.CLIArguments;
import demo.SlackIntegration;
import demo.check.artifact.CallGraphDif;
import demo.check.artifact.SootAnalysis;
import demo.check.code.SpoonAnalysis;
import visual.DemoVisual;
import visual.DumpToJson;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class DetectionEngine {

    private final static Logger LOGGER = Logger.getLogger(DetectionEngine.class.getName());

    public static void run() throws Exception {

        MutableGraph<String> originalCallGraph = constructCallGraph(
                CLIArguments.getInstance().getArtifactPath(),
                CLIArguments.getInstance().getClassName(),
                CLIArguments.getInstance().getMethodName());

        MutableGraph<String> tamperedCallGraph = constructCallGraph(
                CLIArguments.getInstance().getTamperedPath(),
                CLIArguments.getInstance().getClassName(),
                CLIArguments.getInstance().getMethodName());

        MutableGraph<String> difGraph = constructDiffGraph(originalCallGraph, tamperedCallGraph);

        Set<String> sourceCodeMethods = analyzeAST(CLIArguments.getInstance().getSourceCodePath());

        detect(tamperedCallGraph,
                difGraph,
                sourceCodeMethods,
                CLIArguments.getInstance().isToDraw(),
                CLIArguments.getInstance().getOutputJsonPath(),
                CLIArguments.getInstance().getBuildId(),
                CLIArguments.getInstance().isDiffOnly());
    }

    private static void detect(MutableGraph<String> tamperedCallGraph,
                               MutableGraph<String> diffGraph,
                               Set<String> sourceCodeMethods,
                               boolean isToDraw,
                               Path outputJsonPath,
                               String buildId,
                               boolean diffOnly) throws IOException, InterruptedException {

        if (!isTampered(diffGraph, sourceCodeMethods)) {
            LOGGER.info("NO TAMPERING FOUND!");
            return;
        }

        enrichDiffGraph(tamperedCallGraph, diffGraph, sourceCodeMethods);
        drawAndLogTamperedGraphPart(tamperedCallGraph, diffGraph, isToDraw, diffOnly);
        DumpToJson dumpToJson = new DumpToJson(outputJsonPath, tamperedCallGraph, diffGraph, buildId, diffOnly);

        try {
            dumpToJson.dump();
        } catch (IOException e) {
            LOGGER.throwing(DetectionEngine.class.getName(), "detect", e);
            throw e;
        }

        SlackIntegration.sendSlackAlert(buildId);
    }

    private static void drawAndLogTamperedGraphPart(MutableGraph<String> tamperedCallGraph, MutableGraph<String> diffGraph, boolean isToDraw, boolean diffOnly) {
        LOGGER.info("The tampered graph part is:" + diffGraph);

        if (isToDraw) {
            DemoVisual demoVisual = new DemoVisual(tamperedCallGraph, diffGraph);
            demoVisual.visualize(diffOnly);
        }
    }

    private static void enrichDiffGraph(MutableGraph<String> tamperedCallGraph, MutableGraph<String> diffGraph, Set<String> sourceCodeMethods) {
        sourceCodeMethods.forEach(diffGraph::removeNode);
        Set<String> diffNodes = diffGraph.nodes().stream().filter(v -> !v.startsWith("<java")).collect(Collectors.toSet());
        tamperedCallGraph.edges().forEach(v -> {
            if (diffNodes.contains(v.source()) || diffNodes.contains(v.target())) {
                diffGraph.addNode(v.source());
                diffGraph.addNode(v.target());
                diffGraph.putEdge(v.source(), v.target());
            }
        });
    }

    private static MutableGraph<String> constructDiffGraph(MutableGraph<String> originalCallGraph, MutableGraph<String> tamperedCallGraph) {
        CallGraphDif callGraphDif = new CallGraphDif(originalCallGraph, tamperedCallGraph);
        return callGraphDif.compare();
    }

    private static MutableGraph<String> constructCallGraph(Path originalArtifactPath, String entryPointClassName, String entryPointMethodName) {
        SootAnalysis sootAnalysis = new SootAnalysis(originalArtifactPath,
                entryPointClassName,
                entryPointMethodName);
        return sootAnalysis.prepareAndRun();
    }

    private static Set<String> analyzeAST(Path originalArtifactPath) {
        SpoonAnalysis spoonAnalysis = new SpoonAnalysis(originalArtifactPath);
        return spoonAnalysis.analyze();
    }

    private static boolean isTampered(MutableGraph<String> callGraphDif, Set<String> sourceCodeMethods) {
        return !sourceCodeMethods.containsAll(callGraphDif.nodes());
    }
}
