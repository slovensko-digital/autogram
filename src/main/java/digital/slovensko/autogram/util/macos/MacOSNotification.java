package digital.slovensko.autogram.util.macos;

import com.sun.jna.Function;
import com.sun.jna.NativeLibrary;
import com.sun.jna.Pointer;

import digital.slovensko.autogram.util.OperatingSystem;

/**
 * Utility for sending simple notifications on macOS using NSUserNotificationCenter via JNA.
 */
public class MacOSNotification {
    private static final boolean AVAILABLE;
    private static final NativeLibrary OBJC;
    private static final Function OBJC_MSG_SEND;
    private static final Function OBJC_GET_CLASS;
    private static final Function SEL_REGISTER_NAME;

    static {
        boolean ok;
        NativeLibrary objc = null;
        Function msgSend = null;
        Function getClass = null;
        Function selReg = null;
        try {
            if (OperatingSystem.current() == OperatingSystem.MAC) {
                objc = NativeLibrary.getInstance("objc");
                msgSend = objc.getFunction("objc_msgSend");
                getClass = objc.getFunction("objc_getClass");
                selReg = objc.getFunction("sel_registerName");
                // test call
                getClass.invokePointer(new Object[]{"NSObject"});
                ok = true;
            } else {
                ok = false;
            }
        } catch (Throwable t) {
            ok = false;
        }
        AVAILABLE = ok;
        OBJC = objc;
        OBJC_MSG_SEND = msgSend;
        OBJC_GET_CLASS = getClass;
        SEL_REGISTER_NAME = selReg;
    }

    private static Pointer cls(String name) {
        return OBJC_GET_CLASS.invokePointer(new Object[]{name});
    }

    private static Pointer sel(String name) {
        return SEL_REGISTER_NAME.invokePointer(new Object[]{name});
    }

    private static Pointer msg(Pointer receiver, Pointer selector, Object... args) {
        Object[] params = new Object[2 + (args == null ? 0 : args.length)];
        params[0] = receiver;
        params[1] = selector;
        if (args != null) System.arraycopy(args, 0, params, 2, args.length);
        return OBJC_MSG_SEND.invokePointer(params);
    }

    private static Pointer nsString(String s) {
        Pointer cls = cls("NSString");
        Pointer str = msg(cls, sel("alloc"));
        return msg(str, sel("initWithUTF8String:"), s);
    }

    /**
     * Deliver a user notification on macOS. No-op on other platforms or when JNA is unavailable.
     */
    public static void notify(String title, String text) {
        if (!AVAILABLE) {
            return;
        }
        try {
            Pointer notifClass = cls("NSUserNotification");
            Pointer notif = msg(notifClass, sel("alloc"));
            notif = msg(notif, sel("init"));
            msg(notif, sel("setTitle:"), nsString(title));
            msg(notif, sel("setInformativeText:"), nsString(text));

            Pointer centerClass = cls("NSUserNotificationCenter");
            Pointer center = msg(centerClass, sel("defaultUserNotificationCenter"));
            msg(center, sel("deliverNotification:"), notif);
        } catch (Throwable ignored) {
            // ignore failures
        }
    }
}
