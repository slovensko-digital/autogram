package digital.slovensko.autogram.core;

import java.util.Objects;

import eu.europa.esig.dss.enumerations.MimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;

public record AutogramMimeType(
    String string,
    String extension
) implements MimeType {
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

        var withoutSemicolon = mimeTypeString.split(";")[0];
        var string = withoutSemicolon.split("/")[0];
        var extension = withoutSemicolon.split("/")[1];

		return new AutogramMimeType(string, extension);
	}
}
