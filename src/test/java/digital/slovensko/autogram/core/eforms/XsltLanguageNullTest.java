package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class XsltLanguageNullTest {

	@Test
	void selectXsltDoesNotThrowWhenEntryLanguageIsNull() {
		var entries = new ArrayList<ManifestXsltEntry>();
		entries.add(new ManifestXsltEntry(null, null, "HTML", null, "x.xslt", "sign"));

		var result = Assertions.assertDoesNotThrow(
				() -> new EFormResourceLoader().selectXslt(entries, null, "sk", null, null, null, "prefix/"));

		Assertions.assertNull(result);
	}
}
