package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.ManifestXsltEntry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

public class XsltTargetSelectionTest {

    @Test
    void selectXsltKeepsEntryWithRequestedTargetEnvironment() {
        var entries = new ArrayList<ManifestXsltEntry>();
        entries.add(new ManifestXsltEntry(
                "application/xslt+xml", "sk", "HTML", "A", "form.sb.xslt", "sign"));
        entries.add(new ManifestXsltEntry(
                "application/xslt+xml", "sk", "HTML", "B", "form2.sb.xslt", "sign"));

        var selected = new EFormResourceLoader()
                .selectXslt(entries, null, null, "A", null, null, "prefix/");

        Assertions.assertNotNull(selected);
        Assertions.assertEquals("A", selected.target());
    }
}
