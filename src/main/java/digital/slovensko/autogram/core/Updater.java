package digital.slovensko.autogram.core;

import digital.slovensko.autogram.Main;

import java.io.IOException;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.NoSuchElementException;

import static java.util.Objects.requireNonNullElse;

public class Updater {
    private static final String RELEASES_URL = "https://github.com/slovensko-digital/autogram/releases";

    public static boolean newerVersionExists() {
        // if (currentVersion().equals("dev")) {
            // return false;
        // }

        String latestVersion = "";
        try {
            var request = HttpRequest.newBuilder().uri(new URI(RELEASES_URL + "/latest"))
                    .header("Accept", "application/vnd.github.v3+json").GET().build();

            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 302)
                return false;

            var location = response.headers().firstValue("Location").orElseThrow();
            latestVersion = location.substring(location.lastIndexOf('/') + 1);

        } catch (IOException | InterruptedException | URISyntaxException | NoSuchElementException ignored) {
            ignored.printStackTrace(); // TODO handle error
            return false;
        }

        if (latestVersion.equals(""))
            return false;

        if (!currentVersion().equals(latestVersion))
            return true;

        return false;
    }

    private static String currentVersion() {
        return requireNonNullElse(Main.class.getPackage().getImplementationVersion(), "dev");
    }
}
