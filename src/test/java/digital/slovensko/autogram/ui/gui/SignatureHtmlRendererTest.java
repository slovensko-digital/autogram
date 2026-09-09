package digital.slovensko.autogram.ui.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SignatureHtmlRendererTest {
    @Test
    void emptyDocumentUsesLiteralTemplateWithoutFormatterArtifacts() {
        var html = SignatureHtmlRenderer.emptyDocument();

        assertTrue(html.contains("45%"));
        assertTrue(html.contains("55%"));
        assertTrue(html.contains(".summary-list"));
        assertTrue(html.contains("font-size: 16px"));
        assertFalse(html.contains("__AUTOGRAM_SIGNATURE_BODY__"));
        assertFalse(html.contains("<!DOCTYPE html>"));
    }
}