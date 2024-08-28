package demo.check.artifact;

import com.google.common.graph.EndpointPair;
import com.google.common.graph.Graph;
import com.google.common.graph.GraphBuilder;
import com.google.common.graph.MutableGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public class CallGraphDif {

    private final static Logger LOGGER = Logger.getLogger(CallGraphDif.class.getName());

    private final Graph<String> prevGraph;
    private final Graph<String> currentGraph;

    public CallGraphDif(Graph<String> prevGraph, Graph<String> currentGraph) {
        this.prevGraph = prevGraph;
        this.currentGraph = currentGraph;
    }

    public MutableGraph<String> compare() {
        LOGGER.info("Call Graphs comparison started");

        Set<String> newNodes = extractNewNodes();
        return extractDifSubGraph(newNodes);
    }

    private Set<String> extractNewNodes() {
        Set<String> prevNodes = prevGraph.nodes();
        Set<String> currentNodes = currentGraph.nodes();
        Set<String> newNodes = new HashSet<>();
        for (String node : currentNodes) {
            if (!prevNodes.contains(node)) {
                newNodes.add(node);
            }
        }

        return newNodes;
    }

    private MutableGraph<String> extractDifSubGraph(Set<String> newNodes) {
        MutableGraph<String> difSubGraph = GraphBuilder
                .directed()
                .allowsSelfLoops(true)
                .build();

        Set<EndpointPair<String>> currentEdges = currentGraph.edges();
        for (EndpointPair<String> edge: currentEdges) {
            String source = edge.source();
            String target = edge.target();

            if (newNodes.contains(source) || newNodes.contains(target)) {
                difSubGraph.addNode(source);
                difSubGraph.addNode(target);
                difSubGraph.putEdge(source, target);
            }
        }

        LOGGER.info("Call Graphs Diff created");
        return difSubGraph;
    }
}
