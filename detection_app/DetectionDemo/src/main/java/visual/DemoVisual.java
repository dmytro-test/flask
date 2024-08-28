package visual;

import com.google.common.graph.MutableGraph;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.DefaultGraph;
import org.graphstream.ui.geom.Point2;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.swing_viewer.SwingViewer;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.camera.Camera;

import java.awt.*;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressWarnings("UnstableApiUsage")
public class DemoVisual {

    private final MutableGraph<String> callGraph;
    private final MutableGraph<String> diffGraph;

    public DemoVisual(MutableGraph<String> callGraph, MutableGraph<String> diffGraph) {
        System.setProperty("org.graphstream.ui", "swing");
        this.callGraph = callGraph;
        this.diffGraph = diffGraph;
    }

    public void visualize(boolean diffOnly) {
        Graph graph = new DefaultGraph("BlindSpot Graph");
        graph.setAttribute("ui.quality");
        graph.setAttribute("ui.antialias");

        if (diffOnly) {
            prepareDiffGraph(graph);
        } else {
            prepareGraph(graph);
        }

        displayGraph(graph);
    }

    private void prepareGraph(Graph graph) {
        Set<String> diffNodes = diffGraph.nodes();
        Set<String> javaNodes = callGraph.nodes().stream().filter(v -> v.startsWith("<java")).collect(Collectors.toSet());
        javaNodes.forEach(callGraph::removeNode);
        callGraph.nodes().forEach(v -> {
            if (!callGraph.adjacentNodes(v).isEmpty())  {
                Node node = graph.addNode(v);
                node.setAttribute("layout.weight", callGraph.adjacentNodes(v).size());

                if (diffNodes.contains(node.getId())) {
                    addNodeLabel(node);
                    node.setAttribute("ui.style", "fill-color: rgb(0,100,255);");
                }
            }
        });

        Set<String> edges = diffGraph.edges().stream().map(v -> (v.nodeU() + v.nodeV())).collect(Collectors.toSet());
        callGraph.edges().forEach(v -> {
            final String edgeId = v.nodeU() + v.nodeV();
            Edge edge = graph.addEdge(edgeId, v.nodeU(), v.nodeV(), true);
            edge.setAttribute("layout.weight", callGraph.adjacentNodes(v.nodeU()).size());
            if (edges.contains(edgeId)) {
                edge.setAttribute("ui.style", "stroke-width: 30px; fill-color: red;");
            }
        });
    }

    private void prepareDiffGraph(Graph graph) {
        Set<String> diffNodes = diffGraph.nodes();
        Set<String> javaNodes = callGraph.nodes().stream().filter(v -> v.startsWith("<java")).collect(Collectors.toSet());
        javaNodes.forEach(callGraph::removeNode);
        diffGraph.nodes().forEach(v -> {
            if (!callGraph.adjacentNodes(v).isEmpty())  {
                Node node = graph.addNode(v);
                node.setAttribute("layout.weight", callGraph.adjacentNodes(v).size());

                addNodeLabel(node);
                node.setAttribute("ui.style", "fill-color: rgb(0,100,255);");

                callGraph.adjacentNodes(v).forEach(a -> {
                    if (!diffNodes.contains(a)) {
                        Node adjacentNode = graph.addNode(a);
                        addNodeLabel(adjacentNode);

                        final String edgeId = a + v;
                        graph.addEdge(edgeId, a, v, true);
                    }
                });
            }
        });

        diffGraph.edges().forEach(v -> {
            final String edgeId = v.nodeU() + v.nodeV();
            Edge edge = graph.addEdge(edgeId, v.nodeU(), v.nodeV(), true);
            edge.setAttribute("ui.style", "stroke-width: 30px; fill-color: red;");
        });
    }

    private void addNodeLabel(Node node) {
        final String label = node.getId().replaceFirst("<net.schmizz.sshj.", "").replaceFirst(">", "");
        node.setAttribute("label", label);
    }

    private void displayGraph(Graph graph) {
        final Viewer viewer = new SwingViewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        final View view = viewer.addDefaultView(true);
        view.getCamera().setViewPercent(1);

        ((Component) view).addMouseWheelListener(mouseWheelEvent -> {
            mouseWheelEvent.consume();
            int i = mouseWheelEvent.getWheelRotation();
            double factor = Math.pow(1.25, i);
            Camera cam = view.getCamera();
            double zoom = cam.getViewPercent() * factor;
            Point2 pxCenter  = cam.transformGuToPx(cam.getViewCenter().x, cam.getViewCenter().y, 0);
            Point3 guClicked = cam.transformPxToGu(mouseWheelEvent.getX(), mouseWheelEvent.getY());
            double newRatioPx2Gu = cam.getMetrics().ratioPx2Gu/factor;
            double x = guClicked.x + (pxCenter.x - mouseWheelEvent.getX())/newRatioPx2Gu;
            double y = guClicked.y - (pxCenter.y - mouseWheelEvent.getY())/newRatioPx2Gu;
            cam.setViewCenter(x, y, 0);
            cam.setViewPercent(zoom);
        });

        viewer.enableAutoLayout();
    }
}
