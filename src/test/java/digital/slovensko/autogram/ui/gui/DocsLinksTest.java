package digital.slovensko.autogram.ui.gui;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The app links to pages of the user guide by keys ending with ".help" (e.g. error.PINIncorrectException.help), whose
 * values are paths relative to docs.url like "troubleshooting/#nespravny-pin". Checks that every such page and anchor
 * exists in the guide sources in docs/docs/&lt;language&gt;, so renaming a heading there doesn't break a link.
 */
class DocsLinksTest {
    private static final Path GUIDE = Path.of("docs", "docs");
    private static final Pattern LINK = Pattern.compile("([a-z-]+)/(?:#([a-z0-9-]+))?");

    @ParameterizedTest
    @CsvSource({"l10n.properties, sk", "l10n_en.properties, en"})
    void helpLinksPointToExistingSections(String file, String language) throws IOException {
        assumeTrue(Files.isDirectory(GUIDE), "the user guide is not in this checkout");
        var properties = load(file);

        for (var key : helpKeys(properties)) {
            var link = properties.getProperty(key);
            var matcher = LINK.matcher(link);
            assertTrue(matcher.matches(), key + " is not a page/#anchor path: " + link);

            var page = GUIDE.resolve(language).resolve(matcher.group(1) + ".md");
            assertTrue(Files.exists(page), key + " points to a missing page " + page);
            if (matcher.group(2) != null)
                assertTrue(Files.readString(page).contains("{#" + matcher.group(2) + "}"),
                        key + " points to a missing anchor in " + page + ": " + link);
        }
    }

    @Test
    void bothLanguagesLinkTheSameErrors() throws IOException {
        // a key missing in English would fall back to the Slovak anchor, which doesn't exist on the English page
        assertEquals(helpKeys(load("l10n.properties")), helpKeys(load("l10n_en.properties")));
    }

    @Test
    void docsUrlIsInTheLanguageOfTheApp() throws IOException {
        assertEquals("https://docs.autogram.slovensko.digital/", load("l10n.properties").getProperty("docs.url"));
        assertEquals("https://docs.autogram.slovensko.digital/en/", load("l10n_en.properties").getProperty("docs.url"));
    }

    private static Set<String> helpKeys(Properties properties) {
        return properties.stringPropertyNames().stream().filter(k -> k.endsWith(".help")).collect(Collectors.toSet());
    }

    private static Properties load(String file) throws IOException {
        var properties = new Properties();
        try (var stream = DocsLinksTest.class.getResourceAsStream("language/" + file)) {
            properties.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        }
        return properties;
    }
}
