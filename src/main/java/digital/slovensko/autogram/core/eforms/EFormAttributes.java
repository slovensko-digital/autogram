package digital.slovensko.autogram.core.eforms;

import digital.slovensko.autogram.core.eforms.dto.XsltParams;
import eu.europa.esig.dss.enumerations.ASiCContainerType;
import eu.europa.esig.dss.enumerations.SignaturePackaging;

public record EFormAttributes(String identifier, String transformation, String schema, String containerXmlns,
        ASiCContainerType container, SignaturePackaging packaging, String xsdIdentifier, XsltParams xsltParams) {

}
