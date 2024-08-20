package digital.slovensko.autogram.util;

import java.io.IOException;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.pades.exception.InvalidPasswordException;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxDocumentReader;

public class PDFUtils {
    public static boolean isPdfAndPasswordProtected(DSSDocument document) {
        if (!document.getMimeType().equals(MimeTypeEnum.PDF))
            return false;

        try {
            PdfBoxDocumentReader reader = new PdfBoxDocumentReader(document);

            // document is protected against modification without password
            if (!reader.canCreateSignatureField()) {
                reader.close();
                return true;
            }

            reader.close();
        } catch (InvalidPasswordException e) {
            return true;
        } catch (IOException e) {}

        return false;
    }

}
