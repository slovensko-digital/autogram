package com.octosign.whitelabel.communication;

import com.octosign.whitelabel.communication.document.Document;

public record SignedData(Document document, MimeType payloadMimeType) { }
