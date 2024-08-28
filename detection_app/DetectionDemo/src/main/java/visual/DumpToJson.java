package visual;

import com.google.common.graph.Graph;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Logger;

@SuppressWarnings("UnstableApiUsage")
public class DumpToJson {

    private final static Logger LOGGER = Logger.getLogger(DumpToJson.class.getName());

    private final Path jsonPath;
    private final Graph<String> callGraph;
    private final Graph<String> diffGraph;
    private final String buildId;
    private final boolean diffOnly;
    
    private int nodeCounter = 0;

    public DumpToJson(Path jsonPath, Graph<String> callGraph, Graph<String> diffGraph, String buildId, boolean diffOnly) {
        this.jsonPath = jsonPath;
        this.callGraph = callGraph;
        this.diffGraph = diffGraph;
        this.buildId = buildId;
        this.diffOnly = diffOnly;
    }

    public void dump() throws IOException {
        JSONObject json = createJson();
        FileWriter fileWriter = new FileWriter(jsonPath.toString(), false);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        fileWriter.write(gson.toJson(JsonParser.parseString(json.toJSONString())));
        fileWriter.flush();

        LOGGER.info(String.format("Graph was written as json to [%s]", jsonPath));
    }

    @SuppressWarnings("unchecked")
    private JSONObject createJson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("buildId", buildId);
        jsonObject.put("nodes", diffOnly? getNodesJsonArrayDiffOnly() : getNodesJsonArray());
        jsonObject.put("links", diffOnly? getLinksJsonArrayDiffOnly() : getLinksJsonArray());

        return jsonObject;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getLinksJsonArray() {
        JSONArray linksJsonArray = new JSONArray();
        callGraph.edges().forEach(v -> {
            JSONObject link = getLinkJsonObject(v.source(), v.target());
            linksJsonArray.add(link);
        });
        return linksJsonArray;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getLinksJsonArrayDiffOnly() {
        JSONArray linksJsonArray = new JSONArray();
        diffGraph.edges().forEach(v -> {
            JSONObject link = getLinkJsonObject(v.source(), v.target());
            linksJsonArray.add(link);
        });

        Set<String> diffNodes = diffGraph.nodes();
        diffGraph.nodes().forEach(v -> callGraph.adjacentNodes(v).forEach(s -> {
            if (!diffNodes.contains(s)) {
                JSONObject adjacentLink = getLinkJsonObject(s, v);
                linksJsonArray.add(adjacentLink);
            }
        }));
        return linksJsonArray;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getLinkJsonObject(String source, String target) {
        JSONObject link = new JSONObject();
        link.put("source", sanitize(source));
        link.put("target", sanitize(target));
        link.put("value", getLinkValue(source, target));
        return link;
    }

    private String sanitize(String v) {
        return v.replaceAll("[<>]", "");
    }

    private int getLinkValue(String source, String target) {
        int value = 1;
        if (diffGraph.hasEdgeConnecting(source, target)) {
            value = 2;
        }
        return value;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getNodesJsonArray() {
        JSONArray nodesJsonArray = new JSONArray();
        Set<String> diffNodes = diffGraph.nodes();
        callGraph.nodes().forEach(v -> {
            JSONObject nodeJson = getJsonObjectForNode(diffNodes, v);
            nodesJsonArray.add(nodeJson);
        });
        return nodesJsonArray;
    }

    @SuppressWarnings("unchecked")
    private JSONArray getNodesJsonArrayDiffOnly() {
        JSONArray nodesJsonArray = new JSONArray();
        Set<String> diffNodes = diffGraph.nodes();
        diffGraph.nodes().forEach(v -> {
            JSONObject nodeJson = getJsonObjectForNode(diffNodes, v);
            nodesJsonArray.add(nodeJson);
            callGraph.adjacentNodes(v).forEach(s -> {
                if (!diffNodes.contains(s)) {
                    JSONObject successorNodeJson = getJsonObjectForNode(diffNodes, s);
                    nodesJsonArray.add(successorNodeJson);
                }
            });
        });
        return nodesJsonArray;
    }

    @SuppressWarnings("unchecked")
    private JSONObject getJsonObjectForNode(Set<String> diffNodes, String v) {
        JSONObject nodeJson = new JSONObject();
        nodeJson.put("id", nodeCounter++);
        nodeJson.put("name", sanitize(v));
        nodeJson.put("group", getNodeGroup(diffNodes, v));
        return nodeJson;
    }

    private int getNodeGroup(Set<String> diffNodes, String node) {
        int group = 1;
        if (diffNodes.contains(node)) {
            group = 2;
        }
        return group;
    }
}
