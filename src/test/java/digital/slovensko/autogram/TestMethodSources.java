package digital.slovensko.autogram;

import digital.slovensko.autogram.core.AutogramMimeType;
import eu.europa.esig.dss.enumerations.MimeTypeEnum;
import eu.europa.esig.dss.model.DSSDocument;
import eu.europa.esig.dss.model.InMemoryDocument;
import java.util.stream.Stream;
import java.io.IOException;

public abstract class TestMethodSources {
    private static final Class cls = TestMethodSources.class;

    public static Stream<DSSDocument> generalAgendaProvider() throws IOException {
        var inlineXml = cls.getResourceAsStream("general_agenda.xml").readAllBytes();
        var inlineXmlHeader = cls.getResourceAsStream("general_agenda_header.xml").readAllBytes();
        var indentedXml = cls.getResourceAsStream("general_agenda_indented.xml").readAllBytes();
        var indentedXmlHeader = cls.getResourceAsStream("general_agenda_header_indented.xml").readAllBytes();
        var inlineXdc = cls.getResourceAsStream("general_agenda_xdc.xml").readAllBytes();
        var inlineXdcf = cls.getResourceAsStream("general_agenda.xdcf").readAllBytes();
        var indentedXdc = cls.getResourceAsStream("general_agenda_xdc_indented.xml").readAllBytes();
        var inlineAsice = cls.getResourceAsStream("general_agenda.asice").readAllBytes();
        var indentedAsice = cls.getResourceAsStream("general_agenda_indented.asice").readAllBytes();

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
        var inlineXdcf = cls.getResourceAsStream("general_agenda.xdcf").readAllBytes();

        return Stream.of(
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcf.xdcf", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcfBinary.xdcf", MimeTypeEnum.BINARY),
            new InMemoryDocument(inlineXdcf, "generalAgendaInlineXdcfXml.xdcf", MimeTypeEnum.XML)
        );
    }

    public static Stream<DSSDocument> fsDPFOProvider() throws IOException {
        var inlineXml = cls.getResourceAsStream("fs_forms/dic2120515056_fs792_772.xml").readAllBytes();
        var inlineXmlHeader = cls.getResourceAsStream("fs_forms/d_fs792_772_header.xml").readAllBytes();
        var indentedXml = cls.getResourceAsStream("fs_forms/d_fs792_772_indented.xml").readAllBytes();
        var indentedXmlHeader = cls.getResourceAsStream("fs_forms/d_fs792_772_header_indented.xml").readAllBytes();
        var inlineXdc = cls.getResourceAsStream("fs_forms/d_fs792_772_xdc.xml").readAllBytes();
        var indentedXdc = cls.getResourceAsStream("fs_forms/d_fs792_772_xdc_indented.xml").readAllBytes();
        var inlineAsice = cls.getResourceAsStream("fs_forms/signed.asice").readAllBytes();
        var indentedAsice = cls.getResourceAsStream("fs_forms/signed_indented.asice").readAllBytes();
        var timestampedAsice = cls.getResourceAsStream("fs_forms/signed_indented_ts.asice").readAllBytes();

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
        var dpfoAsice = cls.getResourceAsStream("fs_forms/DPFOBv23.asice").readAllBytes();
        var dpfoXdc = cls.getResourceAsStream("fs_forms/unmarked_xdc_indented.xml").readAllBytes();

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
        var wrongXdcSchemaXml = cls.getResourceAsStream("wrong_schema_xdc.xml").readAllBytes();

        return Stream.of(
                new InMemoryDocument(wrongXdcSchemaXml, "wrongXdcSchemaXml.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<DSSDocument> xsdSchemaFailedValidationXmlProvider() throws IOException {
        var wrongSchemaGAXml = cls.getResourceAsStream("wrong_schema_ga.xml").readAllBytes();
        var wrongSchemaGAXdc = cls.getResourceAsStream("wrong_schema_ga_xdc.xml").readAllBytes();

        return Stream.of(
            new InMemoryDocument(wrongSchemaGAXml, "wrongSchemaGAXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(wrongSchemaGAXdc, "wrongSchemaGAXdc.xml", AutogramMimeType.XML_DATACONTAINER)
        );
    }

    public static Stream<DSSDocument> mismatchedDigestsXmlProvider() throws IOException {
        var mismatchedXsdGAXdcXml = cls.getResourceAsStream("mismatched_xsd_ga_xdc.xml").readAllBytes();
        var mismatchedXsltGAXdcXml = cls.getResourceAsStream("mismatched_xslt_ga_xdc.xml").readAllBytes();
        var mismatchedXsdGAXdcAsice = cls.getResourceAsStream("mismatched_xsd_ga_xdc.asice").readAllBytes();
        var mismatchedXsltGAXdcAsice = cls.getResourceAsStream("mismatched_xslt_ga_xdc.asice").readAllBytes();

        return Stream.of(
            new InMemoryDocument(mismatchedXsdGAXdcXml, "mismatchedXsdGAXdcXml.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltGAXdcXml, "mismatchedXsltGAXdcXml.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsdGAXdcAsice, "mismatchedXsdGAXdcAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(mismatchedXsltGAXdcAsice, "mismatchedXsltGAXdcAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> mismatchedDigestsFSXmlProvider() throws IOException {
        var mismatchedXsltFSXdcXml = cls.getResourceAsStream("fs_forms/d_fs792_772_xdc_xsd_digest.xml").readAllBytes();
        var mismatchedXsltFSXsltXdcXml = cls.getResourceAsStream("fs_forms/d_fs792_772_xdc_xslt_digest.xml").readAllBytes();
        var mismatchedXsltFSXdcAsice = cls.getResourceAsStream("fs_forms/signed_xdc_xsd_digest.asice").readAllBytes();
        var mismatchedXsltFSXsltXdcAsice = cls.getResourceAsStream("fs_forms/signed_xdc_xslt_digest.asice").readAllBytes();

        return Stream.of(
            new InMemoryDocument(mismatchedXsltFSXdcXml, "d_fs792_772_xdc_xsd_digest.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltFSXsltXdcXml, "d_fs792_772_xdc_xslt_digest.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(mismatchedXsltFSXdcAsice, "signed_xdc_xsd_digest.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(mismatchedXsltFSXsltXdcAsice, "signed_xdc_xslt_digest.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> unknownEfomXmlProvider() throws IOException {
        var unknownEfomXml = cls.getResourceAsStream("unknown_eform.xml").readAllBytes();
        var unknownEfomXdc = cls.getResourceAsStream("unknown_eform_xdc.xml").readAllBytes();
        var unknownEfomAsice = cls.getResourceAsStream("unknown_eform.asice").readAllBytes();

        return Stream.of(
            new InMemoryDocument(unknownEfomXml, "unknownEfomXml.xml", MimeTypeEnum.XML),
            new InMemoryDocument(unknownEfomXdc, "unknownEfomXdc.xml", AutogramMimeType.XML_DATACONTAINER),
            new InMemoryDocument(unknownEfomAsice, "unknownEfomAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<DSSDocument> invalidAsiceProvider() throws IOException {
        var noSignaturesAsice = cls.getResourceAsStream("no_signatures.asice").readAllBytes();
        var invalidAsice = cls.getResourceAsStream("invalid_asice.asice").readAllBytes();
        var noMetaInfAsice = cls.getResourceAsStream("no_meta_inf.asice").readAllBytes();

        // TODO: implement these asice validations
        // var noManifestAsice = cls.getResourceAsStream("no_manifest.asice").readAllBytes();
        // var noMimetypeAsice = cls.getResourceAsStream("no_mimetype.asice").readAllBytes();

        return Stream.of(
            new InMemoryDocument(noSignaturesAsice, "noSignaturesAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(invalidAsice, "invalidAsice.asice", MimeTypeEnum.ASICE),
            new InMemoryDocument(noMetaInfAsice, "noMetaInfAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> validOtherDocumentsProvider() throws IOException {
        var sampleTxt = cls.getResourceAsStream("sample.txt").readAllBytes();
        var samplePdf = cls.getResourceAsStream("sample.pdf").readAllBytes();
        var samplePng = cls.getResourceAsStream("sample.png").readAllBytes();
        var sampleIco = cls.getResourceAsStream("sample.ico").readAllBytes();
        var sampleDocx = cls.getResourceAsStream("sample.docx").readAllBytes();

        return Stream.of(
            new InMemoryDocument(sampleTxt, "sample.txt", MimeTypeEnum.TEXT),
            new InMemoryDocument(samplePdf, "sample.pdf", MimeTypeEnum.PDF),
            new InMemoryDocument(samplePng, "sample.png", MimeTypeEnum.PNG),
            new InMemoryDocument(sampleIco, "sample.ico", new AutogramMimeType("image/x-icon", "ico")),
            new InMemoryDocument(sampleDocx, "sample.docx", new AutogramMimeType("application/vnd.openxmlformats-officedocument.wordprocessingml.document", "docx"))
        );
    }

    public static Stream<InMemoryDocument> nonEformXmlProvider() throws IOException {
        var nonEformXml = cls.getResourceAsStream("non_eform.xml").readAllBytes();

        return Stream.of(
            new InMemoryDocument(nonEformXml, "nonEformXml.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> pdfForPadesProvider() throws IOException {
        var samplePdf = cls.getResourceAsStream("sample.pdf").readAllBytes();
        var samplePdfSigned = cls.getResourceAsStream("sample_signed.pdf").readAllBytes();

        return Stream.of(
            new InMemoryDocument(samplePdf, "sample.pdf", MimeTypeEnum.PDF),
            new InMemoryDocument(samplePdfSigned, "sample_signed.pdf", MimeTypeEnum.PDF)
        );
    }

    public static Stream<InMemoryDocument> validXadesDocumentsProvider() throws IOException {
        var generalAgendaAsice = cls.getResourceAsStream("general_agenda.asice").readAllBytes();
        var sampleTxtXadesAsice = cls.getResourceAsStream("sample_txt_xades.asice").readAllBytes();
        var samplePdfXadesAsice = cls.getResourceAsStream("sample_pdf_xades.asice").readAllBytes();
        var samplePngXadesAsice = cls.getResourceAsStream("sample_png_xades.asice").readAllBytes();
        var sampleIcoXadesAsice = cls.getResourceAsStream("sample_ico_xades.asice").readAllBytes();
        var sampleDocxXadesAsice = cls.getResourceAsStream("sample_docx_xades.asice").readAllBytes();

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
        var generalAgendaXdcIndented = cls.getResourceAsStream("general_agenda_xdc_indented.xml").readAllBytes();
        var generalAgendaXdc = cls.getResourceAsStream("general_agenda_xdc.xml").readAllBytes();
        var mismatchedXsdGAXdc = cls.getResourceAsStream("mismatched_xsd_ga_xdc.xml").readAllBytes();
        var mismatchedXsltGAXdc = cls.getResourceAsStream("mismatched_xslt_ga_xdc.xml").readAllBytes();
        var unknownEfomXdc = cls.getResourceAsStream("unknown_eform_xdc.xml").readAllBytes();
        var wrongSchemaGAXdc = cls.getResourceAsStream("wrong_schema_ga_xdc.xml").readAllBytes();

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
        var generalAgendaXdcIndented = cls.getResourceAsStream("general_agenda_xdc_indented.xml").readAllBytes();
        var generalAgendaXdc = cls.getResourceAsStream("general_agenda_xdc.xml").readAllBytes();
        var mismatchedXsdGAXdc = cls.getResourceAsStream("mismatched_xsd_ga_xdc.xml").readAllBytes();
        var mismatchedXsltGAXdc = cls.getResourceAsStream("mismatched_xslt_ga_xdc.xml").readAllBytes();
        var unknownEfomXdc = cls.getResourceAsStream("unknown_eform_xdc.xml").readAllBytes();
        var wrongSchemaGAXdc = cls.getResourceAsStream("wrong_schema_ga_xdc.xml").readAllBytes();

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
        var documentContentNoUsedXSDReference = cls.getResourceAsStream("document-content-no-UsedXSDReference.xml").readAllBytes();
        var documentContentUsedXSDReferenceNoAttributes = cls.getResourceAsStream("document-content-UsedXSDReference-no-attributes.xml").readAllBytes();
        var documentContentUsedXSDReferenceNoDigestValue = cls.getResourceAsStream("document-content-UsedXSDReference-no-DigestValue.xml").readAllBytes();
        var emptyXml = cls.getResourceAsStream("empty_xml.xml").readAllBytes();
        var nonEformXml = cls.getResourceAsStream("non_eform.xml").readAllBytes();
        var wrongSchemaXdcXml = cls.getResourceAsStream("wrong_schema_xdc.xml").readAllBytes();

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
        var samplePdfCadesAsice = cls.getResourceAsStream("sample_pdf_cades.asice").readAllBytes();

        return Stream.of(
            new InMemoryDocument(samplePdfCadesAsice, "samplePdfCadesAsice.asice", MimeTypeEnum.ASICE)
        );
    }

    public static Stream<InMemoryDocument> orsrDocumentsProvider() throws  IOException {
        var fupaXml = cls.getResourceAsStream("FUPA.xml").readAllBytes();
        var fupsXml = cls.getResourceAsStream("FUPS.xml").readAllBytes();
        var fuzsNewXml = cls.getResourceAsStream("fuzs_new.xml").readAllBytes();
        var fupsXdcXml = cls.getResourceAsStream("FUPS.xdc.xml").readAllBytes();
        var fupsXdcNoNamespaceXml = cls.getResourceAsStream("FUPS_wo_namespace.xdc.xml").readAllBytes();

        return Stream.of(
                new InMemoryDocument(fupaXml, "FUPA.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXml, "FUPS.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fuzsNewXml, "fuzs_new.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcXml, "FUPS.xdc.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcNoNamespaceXml, "FUPS_wo_namespace.xdc.xml", MimeTypeEnum.XML)
        );
    }

    public static Stream<InMemoryDocument> embeddedOrsrDocumentsProvider() throws IOException {
        var fupsXml = cls.getResourceAsStream("FUPS.xdc.xml").readAllBytes();
        var fupsXdcNoNamespaceXml = cls.getResourceAsStream("FUPS_wo_namespace.xdc.xml").readAllBytes();
        var fupsSignedAsice = cls.getResourceAsStream("FUPS_signed.asice").readAllBytes();
        var fupsXdcSignedAsice = cls.getResourceAsStream("FUPS_xdc_signed.asice").readAllBytes();
        var fupsXdcWoNamespaceSignedAsice = cls.getResourceAsStream("FUPS_wo_namespace_signed.asice").readAllBytes();

        return Stream.of(
                new InMemoryDocument(fupsXml, "FUPS_embedded.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsXdcNoNamespaceXml, "FUPS_embedded_wo_namespace.xdc.xml", MimeTypeEnum.XML),
                new InMemoryDocument(fupsSignedAsice, "FUPS_signed.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(fupsXdcSignedAsice, "FUPS_xdc_signed.asice", MimeTypeEnum.ASICE),
                new InMemoryDocument(fupsXdcWoNamespaceSignedAsice, "FUPS_xdc_wo_namespace_signed.asice", MimeTypeEnum.ASICE)
        );
    }
}
