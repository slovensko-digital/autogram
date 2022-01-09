package com.octosign.whitelabel.ui;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;

import static com.octosign.whitelabel.ui.FXUtils.displayInfo;
import static com.octosign.whitelabel.ui.I18n.translate;

public class UpdateNotifier {
    private static final Map<String, String> ENV = System.getenv();

    private static final String TOKEN = ENV.get("GITHUB_TOKEN");
    private static final String RELEASES_URL = ENV.get("GITHUB_API_URL") + "/repos/" + ENV.get("GITHUB_REPOSITORY") + "/releases";

    private static String currentVersion() {
        return Main.getVersion();
    }

    public static void checkForUpdates() {
        if (currentVersion().equals("dev")) {
            System.out.println("Update check disabled in DEV environment.");
            return;
        }

        HttpRequest request = null;
        try {
            request = HttpRequest.newBuilder().uri(new URI(RELEASES_URL + "/latest"))
                                 .header("Authorization", "token " + TOKEN)
                                 .header("Accept", "application/vnd.github.v3+json")
                                 .GET().build();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }

        HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

        HttpResponse<String> response = null;
        try {
            response = client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ignored) {
            // TODO just log, no app flow interruption needed as this should be only a one-timer
            return;
        }

        JsonObject responseBody = new Gson().fromJson(response.body(), JsonObject.class);
        String latestVersion = responseBody.get("tag_name").getAsString();

        if (!latestVersion.equalsIgnoreCase(currentVersion())) {
            displayInfo("info.updateAvailable.header", translate("info.updateAvailable.description", latestVersion, currentVersion(), RELEASES_URL));
        }
    }

}
