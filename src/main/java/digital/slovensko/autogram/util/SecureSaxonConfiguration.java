package digital.slovensko.autogram.util;

import net.sf.saxon.Configuration;
import net.sf.saxon.functions.FunctionLibraryList;
import net.sf.saxon.functions.MathFunctionSet;
import net.sf.saxon.functions.registry.BuiltInFunctionSet;
import net.sf.saxon.functions.registry.ExsltCommonFunctionSet;
import net.sf.saxon.functions.registry.UseWhen30FunctionSet;
import net.sf.saxon.ma.arrays.ArrayFunctionSet;
import net.sf.saxon.ma.map.MapFunctionSet;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Saxon configuration for stylesheets from untrusted requests, which removes functions that cannot be restricted
 * by configuration properties.
 * <ul>
 *     <li>fn:transform() accepts a saxon:configuration vendor option, which replaces the whole configuration of the
 *     nested transformation and with it all restrictions set in {@link XMLUtils#getSecureTransformerFactory()}.</li>
 *     <li>Vendor functions in the saxon: namespace (saxon:doc() reads documents bypassing the resource resolver and
 *     allowed protocols).</li>
 * </ul>
 */
class SecureSaxonConfiguration extends Configuration {
    private static final Set<String> DENIED_FUNCTIONS = Set.of("transform");

    private final Map<BuiltInFunctionSet, BuiltInFunctionSet> restrictedFunctionSets = new ConcurrentHashMap<>();
    private final UseWhen30FunctionSet restrictedUseWhenFunctionSet = new RestrictedUseWhenFunctionSet();

    @Override
    public BuiltInFunctionSet getXSLTFunctionSet(int version) {
        return restrict(super.getXSLTFunctionSet(version));
    }

    @Override
    public BuiltInFunctionSet getXPathFunctionSet(int version) {
        return restrict(super.getXPathFunctionSet(version));
    }

    @Override
    public synchronized UseWhen30FunctionSet getUseWhenFunctionLibrary(int version) {
        // used for use-when and static variables, evaluated already during stylesheet compilation
        return restrictedUseWhenFunctionSet;
    }

    @Override
    protected FunctionLibraryList makeBuiltInExtensionLibraryList(int version) {
        // same as Configuration, only without VendorFunctionSetHE
        var result = new FunctionLibraryList();
        result.addFunctionLibrary(MathFunctionSet.getInstance());
        result.addFunctionLibrary(MapFunctionSet.getInstance(version));
        result.addFunctionLibrary(ArrayFunctionSet.getInstance(version));
        result.addFunctionLibrary(ExsltCommonFunctionSet.getInstance());
        return result;
    }

    private BuiltInFunctionSet restrict(BuiltInFunctionSet functionSet) {
        return restrictedFunctionSets.computeIfAbsent(functionSet, RestrictedFunctionSet::new);
    }

    private static class RestrictedFunctionSet extends BuiltInFunctionSet {
        RestrictedFunctionSet(BuiltInFunctionSet functionSet) {
            importFunctionSet(functionSet);
        }

        @Override
        public Entry getFunctionDetails(String name, int arity) {
            return DENIED_FUNCTIONS.contains(name) ? null : super.getFunctionDetails(name, arity);
        }
    }

    private static class RestrictedUseWhenFunctionSet extends UseWhen30FunctionSet {
        RestrictedUseWhenFunctionSet() {
            super(31);
        }

        @Override
        public Entry getFunctionDetails(String name, int arity) {
            return DENIED_FUNCTIONS.contains(name) ? null : super.getFunctionDetails(name, arity);
        }
    }
}
