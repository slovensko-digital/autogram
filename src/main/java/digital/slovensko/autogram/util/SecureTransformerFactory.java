package digital.slovensko.autogram.util;

import net.sf.saxon.TransformerFactoryImpl;
import net.sf.saxon.lib.Feature;
import net.sf.saxon.trans.XPathException;

import javax.xml.XMLConstants;
import javax.xml.transform.TransformerConfigurationException;

/**
 * Saxon transformer factory for untrusted stylesheets. It must stay public with a public
 * no-argument constructor, it is also the JAXP default factory, see {@link XMLUtils#useSecureTransformerFactoryByDefault()}.
 */
public class SecureTransformerFactory extends TransformerFactoryImpl {
    public SecureTransformerFactory() throws TransformerConfigurationException {
        super(new SecureSaxonConfiguration());
        setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        var configuration = getConfiguration();
        configuration.setConfigurationProperty(Feature.ALLOWED_PROTOCOLS, "#none");
        configuration.setConfigurationProperty(Feature.ALLOW_EXTERNAL_FUNCTIONS, false);
        configuration.setConfigurationProperty(Feature.DISABLE_XSL_EVALUATE, true);
        configuration.setCollectionFinder((context, collectionURI) -> {
            throw new XPathException("Access to collection " + collectionURI + " is not allowed", "FODC0002");
        });
    }
}
