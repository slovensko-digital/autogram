package digital.slovensko.autogram.core.eforms;

import eu.europa.esig.dss.enumerations.ASiCContainerType;

public record EFormAttributes(String identifier, String transformation, String schema, String containerXmlns,
        ASiCContainerType container) {

}
