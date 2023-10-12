package digital.slovensko.autogram.core;

import java.util.Objects;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public record AutogramMimeType(
    String string,
    String extension
) implements MimeType {
    public static final AutogramMimeType XML_DATACONTAINER = new AutogramMimeType("application/vnd.gov.sk.xmldatacontainer+xml", null);
    public static final AutogramMimeType APPLICATION_XML = new AutogramMimeType("application/xml", null);

    @Override
    public String getMimeTypeString() {
        return string;
    }

    @Override
    public String getExtension() {
        return extension;
    }

    public static MimeType fromMimeTypeString(final String mimeTypeString) {
		Objects.requireNonNull(mimeTypeString, "The mimeTypeString cannot be null!");

        var mimeType = MimeType.fromMimeTypeString(mimeTypeString);
        if (!mimeType.equals(MimeTypeEnum.BINARY))
            return mimeType;

		return new AutogramMimeType(mimeTypeString.split(";")[0], null);
	}

    public static boolean isAsice(MimeType mimeType) {
        return mimeType.equals(MimeTypeEnum.ASICE);
    }

    public static boolean isXML(MimeType mimeType) {
        return mimeType.equals(MimeTypeEnum.XML) || mimeType.equals(APPLICATION_XML);
    }

    public static boolean isXDC(MimeType mimeType) {
        return mimeType.equals(XML_DATACONTAINER);
    }

    public static boolean isPDF(MimeType mimeType) {
        return mimeType.equals(MimeTypeEnum.PDF);
    }
}
