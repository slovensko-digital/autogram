package digital.slovensko.autogram.util;

import java.io.IOException;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.CommonDocument;
import eu.europa.esig.dss.pades.exception.InvalidPasswordException;
import eu.europa.esig.dss.pdf.pdfbox.PdfBoxDocumentReader;

public class PDFUtils {
    public static boolean isPdfAndPasswordProtected(CommonDocument document) {
        if (document.getMimeType().equals(MimeTypeEnum.PDF)) {
            try {
                PdfBoxDocumentReader reader = new PdfBoxDocumentReader(document);
                reader.close();
            } catch (InvalidPasswordException e) {
                return true;
            } catch (IOException e) {
            }
        }
        return false;
    }

}
