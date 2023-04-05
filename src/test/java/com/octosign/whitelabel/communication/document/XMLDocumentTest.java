package com.octosign.whitelabel.communication.document;

import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class XMLDocumentTest {
    @Test
    void testTransformsPlainHtmlWithoutAddingNamespaces() throws IOException {
        var input = this.getClass().getResourceAsStream("abc.xml").readAllBytes();
        var transformation = new String(this.getClass().getResourceAsStream("abc.xslt").readAllBytes());

        XMLDocument doc = new XMLDocument(new Document("id1", "test.xml", input));
        String transformed = doc.getTransformed(transformation);

        assertEquals(-1, transformed.lastIndexOf(":p>"));
    }
}
