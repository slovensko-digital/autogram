package digital.slovensko.autogram.drivers;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import digital.slovensko.autogram.util.Logging;

/**
 * Lists tokens (cards) present in slots (readers) of a PKCS#11 library.
 * <p>
 * Uses the same PKCS#11 module instance as SunPKCS11 provider - modules are cached by library path and
 * initialized only once - so this is safe to call before the token itself is created.
 * <p>
 * Modules are finalized on exit. Otherwise some of them crash the JVM while unloading, e.g. eObčanka driver calls
 * C_Finalize from its own destructor after its internals are already gone. If a module can't be finalized, e.g. it
 * hangs waiting for a stuck PC/SC service, the process is ended without running native destructors at all, as some of
 * them would hang as well (seen with Gemalto driver) and the app would never quit.
 */
final class Pkcs11TokenSlots {
    private static final String PKCS11_CLASS_NAME = "sun.security.pkcs11.wrapper.PKCS11";
    private static final String PKCS11_EXCEPTION_CLASS_NAME = "sun.security.pkcs11.wrapper.PKCS11Exception";
    private static final String CK_C_INITIALIZE_ARGS_CLASS_NAME = "sun.security.pkcs11.wrapper.CK_C_INITIALIZE_ARGS";
    private static final String PKCS11_FUNCTION_LIST = "C_GetFunctionList";
    private static final long CKF_OS_LOCKING_OK = 0x00000002L;
    private static final long CKF_TOKEN_INITIALIZED = 0x00000400L;
    private static final long FINALIZE_TIMEOUT_MILLIS = 5000;

    private static final Logger LOGGER = LoggerFactory.getLogger(Pkcs11TokenSlots.class);
    private static final Set<String> modulesToFinalize = ConcurrentHashMap.newKeySet();
    /** How many times modules were initialized again, by library path */
    private static final Map<String, Integer> moduleGenerations = new ConcurrentHashMap<>();
    /** Modules being initialized, by the thread initializing them */
    private static final Map<Thread, String> modulesInitializing = new ConcurrentHashMap<>();

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(Pkcs11TokenSlots::finalizeModules, "pkcs11-finalizer"));
    }

    private Pkcs11TokenSlots() {
    }

    /**
     * Makes sure the module is finalized on exit, call for every PKCS#11 library used.
     */
    static void finalizeOnExit(String pkcs11Path) {
        modulesToFinalize.add(pkcs11Path);
    }

    private static void finalizeModules() {
        // initialization holds the lock all modules are finalized under, a module still initializing hangs
        if (!modulesInitializing.isEmpty()) {
            LOGGER.warn("PKCS#11 module still initializing: {}", modulesInitializing.values());
            NativeExit.exitImmediately(0);
            return;
        }

        // in parallel, some drivers take a second to finalize, and a hanging driver must not block the others
        var finalizers = new ArrayList<Thread>();
        for (var pkcs11Path : modulesToFinalize) {
            var finalizer = new Thread(() -> finalizeModule(pkcs11Path), "pkcs11-finalizer " + pkcs11Path);
            finalizer.setDaemon(true); // don't block the exit if the driver hangs
            finalizer.start();
            finalizers.add(finalizer);
        }

        var deadline = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(FINALIZE_TIMEOUT_MILLIS);
        var allFinalized = true;
        for (var finalizer : finalizers) {
            try {
                finalizer.join(Math.max(1, TimeUnit.NANOSECONDS.toMillis(deadline - System.nanoTime())));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            if (finalizer.isAlive()) {
                LOGGER.warn("PKCS#11 module not finalized in time: {}", finalizer.getName());
                allFinalized = false;
            }
        }

        // the module's destructor would wait for the hanging finalization
        if (!allFinalized)
            NativeExit.exitImmediately(0);
    }

    private static void finalizeModule(String pkcs11Path) {
        try {
            var pkcs11Class = Class.forName(PKCS11_CLASS_NAME);
            var initArgsClass = Class.forName(CK_C_INITIALIZE_ARGS_CLASS_NAME);
            var getInstance = pkcs11Class.getMethod("getInstance", String.class, String.class, initArgsClass, boolean.class);
            // returns already initialized module, doesn't initialize a new one
            var p11 = invoke(getInstance, null, pkcs11Path, PKCS11_FUNCTION_LIST, null, true);
            invoke(pkcs11Class, p11, "C_Finalize", new Class<?>[]{Object.class}, (Object) null);
        } catch (Exception e) {
            LOGGER.warn("Unable to finalize PKCS#11 module {}: {}", pkcs11Path, e.toString());
        }
    }

    /**
     * Finalizes and initializes again an already loaded module, to get its driver out of a broken state. Sessions and
     * slot IDs of the module are no longer valid then.
     */
    static void reinitializeModule(String pkcs11Path) throws Exception {
        var pkcs11Class = Class.forName(PKCS11_CLASS_NAME);
        var initArgsClass = Class.forName(CK_C_INITIALIZE_ARGS_CLASS_NAME);
        var getInstance = pkcs11Class.getMethod("getInstance", String.class, String.class, initArgsClass, boolean.class);
        // returns already initialized module, SunPKCS11 providers keep using the same instance
        var p11 = invoke(getInstance, null, pkcs11Path, PKCS11_FUNCTION_LIST, null, true);
        invoke(pkcs11Class, p11, "C_Finalize", new Class<?>[]{Object.class}, (Object) null);

        // not public, SunPKCS11 calls it only when loading the module
        var initialize = pkcs11Class.getDeclaredMethod("C_Initialize", Object.class);
        initialize.setAccessible(true);
        var initArgs = initArgsClass.getConstructor().newInstance();
        initArgsClass.getField("flags").setLong(initArgs, CKF_OS_LOCKING_OK);
        try {
            invoke(initialize, p11, initArgs);
        } catch (Exception e) {
            if (!PKCS11_EXCEPTION_CLASS_NAME.equals(e.getClass().getName()))
                throw e;

            invoke(initialize, p11, (Object) null);
        }

        moduleGenerations.merge(pkcs11Path, 1, Integer::sum);
        Logging.log("Reinitialized PKCS#11 module " + pkcs11Path);
    }

    /**
     * Changes whenever the module is initialized again, connections made before then are no longer valid.
     */
    static int getModuleGeneration(String pkcs11Path) {
        return moduleGenerations.getOrDefault(pkcs11Path, 0);
    }

    static List<TokenSlot> listSlotsWithToken(String pkcs11Path) throws Exception {
        var pkcs11Class = Class.forName(PKCS11_CLASS_NAME);
        Object p11;
        modulesInitializing.put(Thread.currentThread(), pkcs11Path);
        try {
            p11 = getModule(pkcs11Class, pkcs11Path);
        } finally {
            modulesInitializing.remove(Thread.currentThread());
        }
        finalizeOnExit(pkcs11Path);

        var slotsWithToken = (long[]) invoke(pkcs11Class, p11, "C_GetSlotList", new Class<?>[]{boolean.class}, true);
        var tokens = new ArrayList<Object[]>();
        for (var slotId : slotsWithToken) {
            try {
                var slotInfo = invoke(pkcs11Class, p11, "C_GetSlotInfo", new Class<?>[]{long.class}, slotId);
                var tokenInfo = invoke(pkcs11Class, p11, "C_GetTokenInfo", new Class<?>[]{long.class}, slotId);
                var tokenFlags = tokenInfo.getClass().getField("flags").getLong(tokenInfo);
                if ((tokenFlags & CKF_TOKEN_INITIALIZED) == 0)
                    continue;

                tokens.add(new Object[]{slotId, slotInfo, tokenInfo});
            } catch (Exception e) {
                // typically a card this driver does not support, e.g. eID card in another reader
                Logging.log("Skipping slot " + slotId + " of " + pkcs11Path + ": " + e);
            }
        }

        // drivers may add slots while reading cards, so the full list is fetched last
        var allSlots = (long[]) invoke(pkcs11Class, p11, "C_GetSlotList", new Class<?>[]{boolean.class}, false);

        var result = new ArrayList<TokenSlot>();
        for (var token : tokens) {
            var slotId = (long) token[0];
            var slotInfo = token[1];
            var tokenInfo = token[2];
            var slotListIndex = indexOf(allSlots, slotId);
            if (slotId > Integer.MAX_VALUE && slotListIndex < 0)
                continue;

            result.add(new TokenSlot(
                    slotId,
                    slotListIndex,
                    getText(tokenInfo, "label"),
                    getText(tokenInfo, "manufacturerID"),
                    getText(tokenInfo, "model"),
                    getText(tokenInfo, "serialNumber"),
                    getText(slotInfo, "slotDescription")));
        }

        Logging.log("Slots of " + pkcs11Path + ": all " + Arrays.toString(allSlots) + ", with token " + result);
        return result;
    }

    private static int indexOf(long[] slots, long slotId) {
        for (int i = 0; i < slots.length; i++) {
            if (slots[i] == slotId)
                return i;
        }

        return -1;
    }

    // mirrors SunPKCS11 initialization: multi-threaded access first, single-threaded as a fallback
    private static Object getModule(Class<?> pkcs11Class, String pkcs11Path) throws Exception {
        var initArgsClass = Class.forName(CK_C_INITIALIZE_ARGS_CLASS_NAME);
        var getInstance = pkcs11Class.getMethod("getInstance", String.class, String.class, initArgsClass, boolean.class);

        var initArgs = initArgsClass.getConstructor().newInstance();
        initArgsClass.getField("flags").setLong(initArgs, CKF_OS_LOCKING_OK);

        try {
            return invoke(getInstance, null, pkcs11Path, PKCS11_FUNCTION_LIST, initArgs, false);
        } catch (Exception e) {
            if (!PKCS11_EXCEPTION_CLASS_NAME.equals(e.getClass().getName()))
                throw e;

            return invoke(getInstance, null, pkcs11Path, PKCS11_FUNCTION_LIST, null, false);
        }
    }

    private static String getText(Object target, String fieldName) throws ReflectiveOperationException {
        var value = (char[]) target.getClass().getField(fieldName).get(target);
        if (value == null)
            return "";

        return new String(value).trim();
    }

    // look up methods on PKCS11 class itself, runtime class may be its non-public subclass
    private static Object invoke(Class<?> pkcs11Class, Object p11, String methodName, Class<?>[] parameterTypes, Object... args) throws Exception {
        return invoke(pkcs11Class.getMethod(methodName, parameterTypes), p11, args);
    }

    private static Object invoke(Method method, Object target, Object... args) throws Exception {
        try {
            return method.invoke(target, args);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof Exception cause)
                throw cause;

            throw e;
        }
    }
}
