package demo;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Logger;

public class SlackIntegration {

    private final static Logger LOGGER = Logger.getLogger(SlackIntegration.class.getName());

    private static final String SLACK_ENDPOINT = "https://blindspot.eu.ngrok.io/api/alert";
    private static final String ALERT_TEXT = ":red_circle: BlindSpot detected code tampering at build [%s]";

    public static void sendSlackAlert(String buildId) throws IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(SLACK_ENDPOINT))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "text/plain")
                .POST(HttpRequest.BodyPublishers.ofString(String.format(ALERT_TEXT, buildId)))
                .build();

        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        LOGGER.info(String.format("SlackEndpoint response is [%s]. Response code is [%d]",
                httpResponse.body(),
                httpResponse.statusCode()));
    }
}
