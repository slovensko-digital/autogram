package digital.slovensko.autogram.util;

import java.io.IOException;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.exception.InvalidPasswordException;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxDocumentReader;

public class PDFUtils {
    public static PDFProtection determinePDFProtection(DSSDocument document) {
        if (!document.getMimeType().equals(MimeTypeEnum.PDF))
            return PDFProtection.NONE;

        try {
            PdfBoxDocumentReader reader = new PdfBoxDocumentReader(document);

            // document is protected against modification without password
            if (!reader.canCreateSignatureField()) {
                reader.close();
                return PDFProtection.MASTER_PASSWORD;
            }

            reader.close();
        } catch (InvalidPasswordException e) {
            return PDFProtection.OPEN_DOCUMENT_PASSWORD;
        } catch (IOException e) {}

        return PDFProtection.NONE;
    }

    public enum PDFProtection {
        NONE,
        OPEN_DOCUMENT_PASSWORD,
        MASTER_PASSWORD,
    }
}
