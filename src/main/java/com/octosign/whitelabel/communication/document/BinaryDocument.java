package com.octosign.whitelabel.communication.document;

import com.octosign.whitelabel.communication.MimeType;

import java.util.Collections;
import java.util.List;


public class BinaryDocument extends Document {
    private static final List<MimeType> FORBIDDEN_MIMETYPES = Collections.emptyList();

    public BinaryDocument(Document document) {
        super(document);
    }

    public static boolean isNotBlacklisted(MimeType mimeType) {
        return FORBIDDEN_MIMETYPES.stream().noneMatch(mimeType::is);
    }
}
