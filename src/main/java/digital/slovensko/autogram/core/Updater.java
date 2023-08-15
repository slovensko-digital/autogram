package digital.slovensko.autogram.core;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.util.Version;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class Updater {
    public static final String LATEST_RELEASE_URL = "https://github.com/slovensko-digital/autogram/releases/latest";
    public static final String LATEST_RELEASE_API_URL = "https://api.github.com/repos/slovensko-digital/autogram/releases/latest";

    public static boolean newVersionAvailable() {
        Version vCurrent = Main.getVersion();
        if (vCurrent.isDev()) {
            return false;
        }

        String latestVersionTag = "";
        try {
            var request = HttpRequest.newBuilder().uri(new URI(LATEST_RELEASE_API_URL)).header("Accept", "application/vnd.github.v3+json").GET().build();

            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var gson = new Gson();
            var json = gson.fromJson(response.body(), JsonObject.class);
            latestVersionTag = json.get("tag_name").getAsString();

        } catch (IOException | InterruptedException | URISyntaxException |
                 NoSuchElementException ignored) {
            ignored.printStackTrace(); // TODO handle error
            return false;
        }

        Version vLatest = Version.createFromVersionString(latestVersionTag);
        return vCurrent.compareTo(vLatest) < 0;
    }
}
