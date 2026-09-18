package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.util.XMLUtils;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import eu.europa.esig.dss.service.http.commons.FileCacheDataLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class EFormResourceLoaderTest {
	private static final String SOURCE_URL = "https://test.example/";
	private static final String FORM_URL = "form";
	private static final String RESOURCE_PATH = SOURCE_URL + FORM_URL + "/Content/";

	@Test
	void getManifestXsltEntriesParsesXsltWhenDestinationTypeDescriptionIsEmpty() throws Exception {
		var htmlXsltUrl = RESOURCE_PATH + "form.UDZSRozhodnutie.html.xslt";
		var signingXsltUrl = RESOURCE_PATH + "form.UDZSRozhodnutie.sb.xslt";
		var dataLoader = new TestDataLoader(Map.of(
				htmlXsltUrl, "PovolenieZdravotnictvo.html.xslt",
				signingXsltUrl, "PovolenieZdravotnictvo.sb.xslt"));
		var resourceLoader = new EFormResourceLoader(dataLoader);

		var entries = resourceLoader.getManifestXsltEntries(
				getManifestFileEntries("manifest_media_destination_type_description_empty.xml"), SOURCE_URL, FORM_URL);

		Assertions.assertEquals(List.of("HTML", "HTML"), entries.stream()
				.map(entry -> entry.destinationType())
				.toList());
		Assertions.assertEquals(List.of(htmlXsltUrl, signingXsltUrl), dataLoader.getRequestedUrls());
	}

	@Test
	void getManifestXsltEntriesUsesDeclaredDestinationTypeDescription() throws Exception {
		var dataLoader = new TestDataLoader(Map.of());
		var resourceLoader = new EFormResourceLoader(dataLoader);

		var entries = resourceLoader.getManifestXsltEntries(
				getManifestFileEntries("manifest_media_destination_type_description_ok.xml"), SOURCE_URL, FORM_URL);

		Assertions.assertEquals(List.of("HTML", "HTML"), entries.stream()
				.map(entry -> entry.destinationType())
				.toList());
		Assertions.assertTrue(dataLoader.getRequestedUrls().isEmpty());
	}

	private static NodeList getManifestFileEntries(String filename) throws Exception {
		try (var input = EFormResourceLoaderTest.class.getResourceAsStream(
				"/digital/slovensko/autogram/crystal_test_data/" + filename)) {
			var document = XMLUtils.getSecureDocumentBuilder().parse(Objects.requireNonNull(input));
			return document.getElementsByTagNameNS("urn:manifest:1.0", "file-entry");
		}
	}

	private static final class TestDataLoader extends FileCacheDataLoader {
		private final Map<String, String> fixturesByUrl;
		private final List<String> requestedUrls = new ArrayList<>();

		private TestDataLoader(Map<String, String> fixturesByUrl) {
			this.fixturesByUrl = fixturesByUrl;
		}

		@Override
		public DSSDocument getDocument(String url) {
			requestedUrls.add(url);

			var fixture = fixturesByUrl.get(url);
			if (fixture == null)
				return null;

			try (var input = EFormResourceLoaderTest.class.getResourceAsStream(
					"/digital/slovensko/autogram/crystal_test_data/" + fixture)) {
				return new InMemoryDocument(Objects.requireNonNull(input).readAllBytes(), fixture);
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}

		private List<String> getRequestedUrls() {
			return requestedUrls;
		}
	}
}