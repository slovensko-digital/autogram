package digital.slovensko.autogram.core;

import digital.slovensko.autogram.Main;
import digital.slovensko.autogram.util.Version;

import java.io.*;
import java.net.*;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.*;
import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import java.util.NoSuchElementException;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

public class Updater {
    public static final String LATEST_RELEASE_URL = "https://sluzby.slovensko.digital/autogram/?utm_source=autogram&utm_medium=application&utm_campaign=autogramupdate#download";
    public static final String LATEST_RELEASE_API_URL = "https://api.github.com/repos/slovensko-digital/autogram/releases/latest";
    
    private static JsonObject latestReleaseInfo = null;
    private static String downloadUrl = null;
    
    public static class UpdateInfo {
        public final String version;
        public final String downloadUrl;
        public final String releaseNotes;
        public final long fileSize;
        
        public UpdateInfo(String version, String downloadUrl, String releaseNotes, long fileSize) {
            this.version = version;
            this.downloadUrl = downloadUrl;
            this.releaseNotes = releaseNotes;
            this.fileSize = fileSize;
        }
    }

    public static boolean newVersionAvailable() {
        return getUpdateInfo() != null;
    }
    
    public static UpdateInfo getUpdateInfo() {
        Version vCurrent = Main.getVersion();
        if (vCurrent.isDev()) {
            return null;
        }

        try {
            var request = HttpRequest.newBuilder()
                .uri(new URI(LATEST_RELEASE_API_URL))
                .header("Accept", "application/vnd.github.v3+json")
                .GET().build();

            var client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
            var response = client.send(request, HttpResponse.BodyHandlers.ofString());
            var gson = new Gson();
            latestReleaseInfo = gson.fromJson(response.body(), JsonObject.class);
            
            String latestVersionTag = latestReleaseInfo.get("tag_name").getAsString();
            Version vLatest = Version.createFromVersionString(latestVersionTag);
            
            if (vCurrent.compareTo(vLatest) >= 0) {
                return null;
            }
            
            // Find macOS download URL
            JsonArray assets = latestReleaseInfo.getAsJsonArray("assets");
            String macOSDownloadUrl = null;
            long fileSize = 0;
            
            for (int i = 0; i < assets.size(); i++) {
                JsonObject asset = assets.get(i).getAsJsonObject();
                String name = asset.get("name").getAsString().toLowerCase();
                if (name.contains("macos") || name.contains("mac") || name.endsWith(".dmg")) {
                    macOSDownloadUrl = asset.get("browser_download_url").getAsString();
                    fileSize = asset.get("size").getAsLong();
                    break;
                }
            }
            
            if (macOSDownloadUrl == null) {
                // Fallback to web download
                macOSDownloadUrl = LATEST_RELEASE_URL;
            }
            
            String releaseNotes = latestReleaseInfo.has("body") ? 
                latestReleaseInfo.get("body").getAsString() : "";
            
            downloadUrl = macOSDownloadUrl;
            return new UpdateInfo(latestVersionTag, macOSDownloadUrl, releaseNotes, fileSize);

        } catch (IOException | InterruptedException | URISyntaxException |
                 NoSuchElementException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public static CompletableFuture<Path> downloadUpdate(UpdateInfo updateInfo, Consumer<Double> progressCallback) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                // Create downloads directory
                Path downloadsDir = Paths.get(System.getProperty("user.home"), "Downloads");
                Path updateFile = downloadsDir.resolve("Autogram-" + updateInfo.version + ".dmg");
                
                // Download the file
                URL url = new URL(updateInfo.downloadUrl);
                URLConnection connection = url.openConnection();
                long totalSize = updateInfo.fileSize > 0 ? updateInfo.fileSize : connection.getContentLengthLong();
                
                try (InputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream out = new FileOutputStream(updateFile.toFile())) {
                    
                    byte[] buffer = new byte[8192];
                    long downloaded = 0;
                    int bytesRead;
                    
                    while ((bytesRead = in.read(buffer)) != -1) {
                        out.write(buffer, 0, bytesRead);
                        downloaded += bytesRead;
                        
                        if (totalSize > 0 && progressCallback != null) {
                            double progress = (double) downloaded / totalSize;
                            progressCallback.accept(progress);
                        }
                    }
                }
                
                return updateFile;
                
            } catch (IOException e) {
                throw new RuntimeException("Failed to download update", e);
            }
        });
    }
    
    public static void installUpdate(Path updateFile) throws IOException {
        if (!Files.exists(updateFile)) {
            throw new IOException("Update file not found: " + updateFile);
        }
        
        // Open the DMG file with the default application (usually Finder)
        ProcessBuilder pb = new ProcessBuilder("open", updateFile.toString());
        pb.start();
    }
    
    public static String getDownloadUrl() {
        return downloadUrl != null ? downloadUrl : LATEST_RELEASE_URL;
    }
}
