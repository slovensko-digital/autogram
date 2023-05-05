package digital.slovensko.autogram.core;

import digital.slovensko.autogram.Main;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.NoSuchElementException;

public class Updater {
    public static final String LATEST_RELEASE_URL = "https://github.com/slovensko-digital/autogram/releases/latest";

    public static boolean newVersionAvailable() {
        if (Main.getVersion().equals("dev")) {
            return false;
        }

        String latestVersionTag = "";
        try {
            var request = HttpRequest.newBuilder().uri(new URI(LATEST_RELEASE_URL))
                    .header("Accept", "application/vnd.github.v3+json").GET().build();

            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 302)
                return false;

            var location = response.headers().firstValue("Location").orElseThrow();
            latestVersionTag = location.substring(location.lastIndexOf('/') + 1);

        } catch (IOException | InterruptedException | URISyntaxException | NoSuchElementException ignored) {
            ignored.printStackTrace(); // TODO handle error
            return false;
        }

        if (latestVersionTag.equals(""))
            return false;

        var runningVersionTag = "v" + Main.getVersion();
        return !runningVersionTag.equals(latestVersionTag);
    }
}
