package demo.check.artifact;

import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;
import soot.*;
import soot.jimple.spark.SparkTransformer;
import soot.jimple.toolkits.callgraph.CHATransformer;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;
import soot.util.Chain;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.Logger;


@SuppressWarnings("UnstableApiUsage")
public class SootAnalysis {

    private final static Logger LOGGER = Logger.getLogger(SootAnalysis.class.getName());

    private static final List<String> JDK_EXCLUDE_LIST = Arrays.asList("java.", "javax.", "sun.", "sunw.", "com.sun.", "com.ibm.");
    private static final List<String> ADDITIONAL_EXCLUDE_LIST = Arrays.asList("org.", "com.jcraft.", "net.i2p.");


    private final Path artifactDirectory;
    private final String className;
    private final String methodName;

    private MutableGraph<String> callGraph;

    public SootAnalysis(Path artifactPath, String className, String methodName) {
        this.artifactDirectory = artifactPath;
        this.className = className;
        this.methodName = methodName;
    }

    public MutableGraph<String> prepareAndRun() {
        setup();
        analyze();

        return callGraph;
    }

    private void setup() {
        configureSoot();

        SootClass entryClass = Scene.v().loadClassAndSupport(className);
        SootMethod entryMethod = entryClass.getMethod(methodName);
        Scene.v().setEntryPoints(Collections.singletonList(entryMethod));
        Scene.v().loadNecessaryClasses();

        enableCHACallGraph();

        LOGGER.info("Soot setup finished");
    }

    private void configureSoot() {
        G.reset();

        Options.v().set_prepend_classpath(true);
        Options.v().set_process_dir(Collections.singletonList(artifactDirectory.toString()));
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_src_prec(Options.src_prec_only_class);
        Options.v().set_whole_program(true);
        Options.v().set_exclude(getTotalExcludeList());
        Options.v().set_no_bodies_for_excluded(true);
    }

    private List<String> getTotalExcludeList() {
        List<String> totalExcludeList = new ArrayList<>();
        totalExcludeList.addAll(JDK_EXCLUDE_LIST);
        totalExcludeList.addAll(ADDITIONAL_EXCLUDE_LIST);
        return totalExcludeList;
    }

    public void analyze() {
        PackManager.v().getPack("wjtp").add(new Transform("wjtp.CalGraphTransform", new SceneTransformer() {

            @Override
            protected void internalTransform(String phaseName, Map options) {
                CallGraph sootCallGraph = Scene.v().getCallGraph();
                callGraph = constructGraph(sootCallGraph);
            }
        }));

        PackManager.v().runPacks();
    }

    @SuppressWarnings("unused")
    private static void enableSparkCallGraph() {
        Map<String, String> phaseOptions = new HashMap<>();
        phaseOptions.put("on-fly-cg", "true");
        SparkTransformer.v().transform("", phaseOptions);
        PhaseOptions.v().setPhaseOption("cg.spark", "enabled:true");
    }

    private static void enableCHACallGraph() {
        CHATransformer.v().transform();
    }

    private MutableGraph<String> constructGraph(CallGraph sootCallGraph) {
        MutableGraph<String> callGraph = GraphBuilder.
                directed().
                allowsSelfLoops(true).
                build();

        Chain<SootClass> applicationClasses = Scene.v().getApplicationClasses();
        LOGGER.info(String.format("Analyzing [%d] classes for CallGraph construction", applicationClasses.size()));
        for (SootClass sootClass : applicationClasses) {
            for (SootMethod fromMethod : sootClass.getMethods()) {

                String fromMethodSignature = fromMethod.getSignature();
                callGraph.addNode(fromMethodSignature);

                Iterator<MethodOrMethodContext> targets = new Targets(
                        sootCallGraph.edgesOutOf(fromMethod));

                while (targets.hasNext()) {
                    SootMethod toMethod = (SootMethod) targets.next();
                    if (toMethod.isJavaLibraryMethod()) {
                        continue;
                    }

                    String toMethodSignature = toMethod.getSignature();
                    callGraph.addNode(toMethodSignature);
                    callGraph.putEdge(fromMethodSignature, toMethodSignature);

                    //System.out.println(fromMethod + " may call " + toMethod);
                }
            }
        }

        return callGraph;
    }
}
