package digital.slovensko.autogram;

import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import java.util.stream.Stream;

import digital.slovensko.autogram.core.dto.AutogramMimeType;

import java.io.IOException;
import java.io.InputStream;

public abstract class TestMethodSources {
    public static byte[] loadContent(String resourceName) throws IOException {
        return loadContentStream(resourceName).readAllBytes();
    }

    public static InputStream loadContentStream(String resourceName) throws IOException {
        return TestMethodSources.class.getResourceAsStream(resourceName);
    }

    public static Stream<DSSDocument> generalAgendaProvider() throws IOException {
        var inlineXml = loadContent("general_agenda.xml");
        var inlineXmlHeader = loadContent("general_agenda_header.xml");
        var indentedXml = loadContent("general_agenda_indented.xml");
        var indentedXmlHeader = loadContent("general_agenda_header_indented.xml");
        var inlineXdc = loadContent("general_agenda_xdc.xml");
        var inlineXdcf = loadContent("general_agenda.xdcf");
        var indentedXdc = loadContent("general_agenda_xdc_indented.xml");
        var inlineAsice = loadContent("general_agenda.asice");
        var indentedAsice = loadContent("general_agenda_indented.asice");

        return Stream.of(
            new InMemoryDocument(inlineXml, "generalAgendaInlineXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(inlineXmlHeader, "generalAgendaInlineXmlHeader.xml", MimeTypeEnum.XML),
            new InMemoryDocument(indentedXml, "generalAgendaIndentedXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(indentedXmlHeader, "generalAgendaIndentedXmlHeader.xml", MimeTypeEnum.XML),
            new InMemoryDocument(inlineXdc, "generalAgendaInlineXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcf.xdcf", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(indentedXdc, "generalAgendaIndentedXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(inlineAsice, "generalAgendaInlineAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(indentedAsice, "generalAgendaIndentedAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> unsetXdcfMimetypeProvider() throws IOException {
        var inlineXdcf = loadContent("general_agenda.xdcf");

        return Stream.of(
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcf.xdcf", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcfBinary.xdcf", MimeTypeEnum.BINARY),
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcfXml.xdcf", MimeTypeEnum.XML)
        );
    }

    public static Stream<DSSDocument> fsDPFOProvider() throws IOException {
        var inlineXml = loadContent("fs_forms/dic2120515056_fs792_772.xml");
        var inlineXmlHeader = loadContent("fs_forms/d_fs792_772_header.xml");
        var indentedXml = loadContent("fs_forms/d_fs792_772_indented.xml");
        var indentedXmlHeader = loadContent("fs_forms/d_fs792_772_header_indented.xml");
        var inlineXdc = loadContent("fs_forms/d_fs792_772_xdc.xml");
        var indentedXdc = loadContent("fs_forms/d_fs792_772_xdc_indented.xml");
        var inlineAsice = loadContent("fs_forms/signed.asice");
        var indentedAsice = loadContent("fs_forms/signed_indented.asice");
        var timestampedAsice = loadContent("fs_forms/signed_indented_ts.asice");

        return Stream.of(
            new InMemoryDocument(inlineXml, "dic2120515056_fs792_772.xml", MimeTypeEnum.XML),
            new InMemoryDocument(inlineXmlHeader, "d_fs792_772_header.xml", MimeTypeEnum.XML),
            new InMemoryDocument(indentedXml, "d_fs792_772_indented.xml", MimeTypeEnum.XML),
            new InMemoryDocument(indentedXmlHeader, "d_fs792_772_header_indented.xml", MimeTypeEnum.XML),
            new InMemoryDocument(inlineXdc, "d_fs792_772_xdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(indentedXdc, "d_fs792_772_xdc_indented.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(inlineAsice, "signed.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(inlineAsice, "d_fs792_772_signed.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(indentedAsice, "signed_indented.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(timestampedAsice, "signed_indented_ts.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> fsUnmarkedXdcProvider() throws IOException {
        var dpfoAsice = loadContent("fs_forms/DPFOBv23.asice");
        var dpfoXdc = loadContent("fs_forms/unmarked_xdc_indented.xml");

        return Stream.of(
            new InMemoryDocument(dpfoAsice, "DPFOBv23.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(dpfoXdc, "unmarked_xdc_indented.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<DSSDocument> invalidXmlProvider() throws IOException {
        var notAnXml = "not an xml".getBytes();
        var invalidXml = "<invalidXml><foo><bar></foo></bar></invalidXml>".getBytes();
        var invalidXmlHeader = "<?xml versionWrong=\"1.0\" encoding=\"UTF-8\"?><invalidXml><foo></foo><bar></bar></invalidXml>".getBytes();
        var invalidXdc = "<invalidXml xmlns=\"http://data.gov.sk/def/container/xmldatacontainer+xml/1.1\"><foo></foo><bar></bar></invalidXml>".getBytes();
        var twoRootElements = "<invalidXml><foo></foo></invalidXml><bar></bar>".getBytes();
        var twoRootElementsHeader = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><invalidXml><foo></foo></invalidXml><bar></bar>".getBytes();


        return Stream.of(
            new InMemoryDocument(notAnXml, "notAnXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(invalidXml, "invalidXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(invalidXmlHeader, "invalidXmlHeader.xml", MimeTypeEnum.XML),
            new InMemoryDocument(invalidXdc, "invalidXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(twoRootElements, "twoRootElements.xml", MimeTypeEnum.XML),
            new InMemoryDocument(twoRootElementsHeader, "twoRootElementsHeader.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<DSSDocument> nonEFormXmlProvider() throws IOException {
        var wrongXdcSchemaXml = loadContent("wrong_schema_xdc.xml");

        return Stream.of(
                new InMemoryDocument(wrongXdcSchemaXml, "wrongXdcSchemaXml.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<DSSDocument> xsdSchemaFailedValidationXmlProvider() throws IOException {
        var wrongSchemaGAXml = loadContent("wrong_schema_ga.xml");
        var wrongSchemaGAXdc = loadContent("wrong_schema_ga_xdc.xml");

        return Stream.of(
            new InMemoryDocument(wrongSchemaGAXml, "wrongSchemaGAXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(wrongSchemaGAXdc, "wrongSchemaGAXdc.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<DSSDocument> mismatchedDigestsXmlProvider() throws IOException {
        var mismatchedXsdGAXdcXml = loadContent("mismatched_xsd_ga_xdc.xml");
        var mismatchedXsltGAXdcXml = loadContent("mismatched_xslt_ga_xdc.xml");
        var mismatchedXsdGAXdcAsice = loadContent("mismatched_xsd_ga_xdc.asice");
        var mismatchedXsltGAXdcAsice = loadContent("mismatched_xslt_ga_xdc.asice");

        return Stream.of(
            new InMemoryDocument(mismatchedXsdGAXdcXml, "mismatchedXsdGAXdcXml.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltGAXdcXml, "mismatchedXsltGAXdcXml.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsdGAXdcAsice, "mismatchedXsdGAXdcAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(mismatchedXsltGAXdcAsice, "mismatchedXsltGAXdcAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> mismatchedDigestsFSXmlProvider() throws IOException {
        var mismatchedXsltFSXdcXml = loadContent("fs_forms/d_fs792_772_xdc_xsd_digest.xml");
        var mismatchedXsltFSXsltXdcXml = loadContent("fs_forms/d_fs792_772_xdc_xslt_digest.xml");
        var mismatchedXsltFSXdcAsice = loadContent("fs_forms/signed_xdc_xsd_digest.asice");
        var mismatchedXsltFSXsltXdcAsice = loadContent("fs_forms/signed_xdc_xslt_digest.asice");

        return Stream.of(
            new InMemoryDocument(mismatchedXsltFSXdcXml, "d_fs792_772_xdc_xsd_digest.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltFSXsltXdcXml, "d_fs792_772_xdc_xslt_digest.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltFSXdcAsice, "signed_xdc_xsd_digest.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(mismatchedXsltFSXsltXdcAsice, "signed_xdc_xslt_digest.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> unknownEfomXmlProvider() throws IOException {
        var unknownEfomXml = loadContent("unknown_eform.xml");
        var unknownEfomXdc = loadContent("unknown_eform_xdc.xml");
        var unknownEfomAsice = loadContent("unknown_eform.asice");

        return Stream.of(
            new InMemoryDocument(unknownEfomXml, "unknownEfomXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(unknownEfomXdc, "unknownEfomXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(unknownEfomAsice, "unknownEfomAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> invalidAsiceProvider() throws IOException {
        var noSignaturesAsice = loadContent("no_signatures.asice");
        var invalidAsice = loadContent("invalid_asice.asice");
        var noMetaInfAsice = loadContent("no_meta_inf.asice");

        // TODO: implement these asice validations
        // var noManifestAsice = loadContent("no_manifest.asice");
        // var noMimetypeAsice = loadContent("no_mimetype.asice");

        return Stream.of(
            new InMemoryDocument(noSignaturesAsice, "noSignaturesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(invalidAsice, "invalidAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(noMetaInfAsice, "noMetaInfAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> validOtherDocumentsProvider() throws IOException {
        var sampleTxt = loadContent("sample.txt");
        var samplePdf = loadContent("sample.pdf");
        var samplePng = loadContent("sample.png");
        var sampleIco = loadContent("sample.ico");
        var sampleDocx = loadContent("sample.docx");

        return Stream.of(
            new InMemoryDocument(sampleTxt, "sample.txt", MimeTypeEnum.TEXT),
            new InMemoryDocument(samplePdf, "sample.pdf", MimeTypeEnum.PDF),
            new InMemoryDocument(samplePng, "sample.png", MimeTypeEnum.PNG),
            new InMemoryDocument(sampleIco, "sample.ico", new AutogramMimeType("image/x-icon", "ico")),
            new InMemoryDocument(sampleDocx, "sample.docx", new AutogramMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"))
        );
    }

    public static Stream<InMemoryDocument> nonEformXmlProvider() throws IOException {
        var nonEformXml = loadContent("non_eform.xml");

        return Stream.of(
            new InMemoryDocument(nonEformXml, "nonEformXml.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> pdfForPadesProvider() throws IOException {
        var samplePdf = loadContent("sample.pdf");
        var samplePdfSigned = loadContent("sample_signed.pdf");

        return Stream.of(
            new InMemoryDocument(samplePdf, "sample.pdf", MimeTypeEnum.PDF),
            new InMemoryDocument(samplePdfSigned, "sample_signed.pdf", MimeTypeEnum.PDF)
        );
    }

    public static Stream<InMemoryDocument> validXadesDocumentsProvider() throws IOException {
        var generalAgendaAsice = loadContent("general_agenda.asice");
        var sampleTxtXadesAsice = loadContent("sample_txt_xades.asice");
        var samplePdfXadesAsice = loadContent("sample_pdf_xades.asice");
        var samplePngXadesAsice = loadContent("sample_png_xades.asice");
        var sampleIcoXadesAsice = loadContent("sample_ico_xades.asice");
        var sampleDocxXadesAsice = loadContent("sample_docx_xades.asice");

        return Stream.of(
            new InMemoryDocument(generalAgendaAsice, "generalAgendaAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(sampleTxtXadesAsice, "sampleTxtXadesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(samplePdfXadesAsice, "samplePdfXadesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(samplePngXadesAsice, "samplePngXadesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(sampleIcoXadesAsice, "sampleIcoXadesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(sampleDocxXadesAsice, "sampleDocxXadesAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> xdcDocumentsProvider() throws IOException {
        var generalAgendaXdcIndented = loadContent("general_agenda_xdc_indented.xml");
        var generalAgendaXdc = loadContent("general_agenda_xdc.xml");
        var mismatchedXsdGAXdc = loadContent("mismatched_xsd_ga_xdc.xml");
        var mismatchedXsltGAXdc = loadContent("mismatched_xslt_ga_xdc.xml");
        var unknownEfomXdc = loadContent("unknown_eform_xdc.xml");
        var wrongSchemaGAXdc = loadContent("wrong_schema_ga_xdc.xml");

        return Stream.of(
            new InMemoryDocument(generalAgendaXdcIndented, "generalAgendaXdcIndented.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(generalAgendaXdc, "generalAgendaXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsdGAXdc, "mismatchedXsdGAXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltGAXdc, "mismatchedXsltGAXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(unknownEfomXdc, "unknownEfomXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(wrongSchemaGAXdc, "wrongSchemaGAXdc.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<InMemoryDocument> xdcDocumentsWithXmlMimetypeProvider() throws IOException {
        var generalAgendaXdcIndented = loadContent("general_agenda_xdc_indented.xml");
        var generalAgendaXdc = loadContent("general_agenda_xdc.xml");
        var mismatchedXsdGAXdc = loadContent("mismatched_xsd_ga_xdc.xml");
        var mismatchedXsltGAXdc = loadContent("mismatched_xslt_ga_xdc.xml");
        var unknownEfomXdc = loadContent("unknown_eform_xdc.xml");
        var wrongSchemaGAXdc = loadContent("wrong_schema_ga_xdc.xml");

        return Stream.of(
            new InMemoryDocument(generalAgendaXdcIndented, "generalAgendaXdcIndented.xml", MimeTypeEnum.XML),
            new InMemoryDocument(generalAgendaXdc, "generalAgendaXdc.xml", MimeTypeEnum.XML),
            new InMemoryDocument(mismatchedXsdGAXdc, "mismatchedXsdGAXdc.xml", MimeTypeEnum.XML),
            new InMemoryDocument(mismatchedXsltGAXdc, "mismatchedXsltGAXdc.xml", MimeTypeEnum.XML),
            new InMemoryDocument(unknownEfomXdc, "unknownEfomXdc.xml", MimeTypeEnum.XML),
            new InMemoryDocument(wrongSchemaGAXdc, "wrongSchemaGAXdc.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> nonXdcXmlDocumentsProvider() throws IOException {
        var documentContentNoUsedXSDReference = loadContent("document-content-no-UsedXSDReference.xml");
        var documentContentUsedXSDReferenceNoAttributes = loadContent("document-content-UsedXSDReference-no-attributes.xml");
        var documentContentUsedXSDReferenceNoDigestValue = loadContent("document-content-UsedXSDReference-no-DigestValue.xml");
        var emptyXml = loadContent("empty_xml.xml");
        var nonEformXml = loadContent("non_eform.xml");
        var wrongSchemaXdcXml = loadContent("wrong_schema_xdc.xml");

        return Stream.of(
            new InMemoryDocument(documentContentNoUsedXSDReference, "documentContentNoUsedXSDReference.xml", MimeTypeEnum.XML),
            new InMemoryDocument(documentContentUsedXSDReferenceNoAttributes, "documentContentUsedXSDReferenceNoAttributes.xml", MimeTypeEnum.XML),
            new InMemoryDocument(documentContentUsedXSDReferenceNoDigestValue, "documentContentUsedXSDReferenceNoDigestValue.xml", MimeTypeEnum.XML),
            new InMemoryDocument(emptyXml, "emptyXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(nonEformXml, "nonEformXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(wrongSchemaXdcXml, "wrongSchemaXdcXml.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> validCadesDocumentsProvider() throws IOException {
        var samplePdfCadesAsice = loadContent("sample_pdf_cades.asice");

        return Stream.of(
            new InMemoryDocument(samplePdfCadesAsice, "samplePdfCadesAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> orsrDocumentsProvider() throws  IOException {
        var fupaXml = loadContent("FUPA.xml");
        var fupsXml = loadContent("FUPS.xml");
        var fuzsNewXml = loadContent("fuzs_new.xml");
        var fupsXdcXml = loadContent("FUPS.xdc.xml");
        var fupsXdcNoNamespaceXml = loadContent("FUPS_wo_namespace.xdc.xml");

        return Stream.of(
                new InMemoryDocument(fupaXml, "FUPA.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXml, "FUPS.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fuzsNewXml, "fuzs_new.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcXml, "FUPS.xdc.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcNoNamespaceXml, "FUPS_wo_namespace.xdc.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> multiDocumentAsiceProvider() throws IOException {
        var basic = loadContent("multi_document/basic.asice");
        var formPdfDifferent = loadContent("multi_document/form_pdf_different.asice");
        var gaFupsMulti = loadContent("multi_document/ga_fups_multi.asice");
        var gaPdfNoSigned = loadContent("multi_document/ga_pdf_no_signed.asice");

        return Stream.of(
                new InMemoryDocument(basic, "basic.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(formPdfDifferent, "form_pdf_different.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(gaFupsMulti, "ga_fups_multi.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(gaPdfNoSigned, "ga_pdf_no_signed.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> embeddedOrsrDocumentsProvider() throws IOException {
        var fupsXml = loadContent("FUPS.xdc.xml");
        var fupsXdcNoNamespaceXml = loadContent("FUPS_wo_namespace.xdc.xml");
        var fupsSignedAsice = loadContent("FUPS_signed.asice");
        var fupsXdcSignedAsice = loadContent("FUPS_xdc_signed.asice");
        var fupsXdcWoNamespaceSignedAsice = loadContent("FUPS_wo_namespace_signed.asice");

        return Stream.of(
                new InMemoryDocument(fupsXml, "FUPS_embedded.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcNoNamespaceXml, "FUPS_embedded_wo_namespace.xdc.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsSignedAsice, "FUPS_signed.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(fupsXdcSignedAsice, "FUPS_xdc_signed.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(fupsXdcWoNamespaceSignedAsice, "FUPS_xdc_wo_namespace_signed.asice", MimeTypeEnum.ASICE)
        );
    }
}
