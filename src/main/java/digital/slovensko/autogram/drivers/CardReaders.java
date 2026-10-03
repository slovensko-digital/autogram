package digital.slovensko.autogram.drivers;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;

import digital.slovensko.autogram.util.Logging;

/**
 * Checks smart card readers for inserted cards via PC/SC, before any PKCS#11 driver is loaded.
 * <p>
 * Drivers of cards talk to readers via PC/SC too, so without a card they have nothing to find. And when the PC/SC
 * service is stuck (seen on macOS), the drivers hang in it - in C_Initialize or C_GetSlotList - and once loaded, some
 * of them also hang the app on exit. Loading them can be avoided then.
 */
public final class CardReaders {
    public enum State {
        /** A card is inserted in some reader. */
        CARD_PRESENT,
        /** No reader, or no card in any reader. */
        NO_CARD,
        /** PC/SC service doesn't respond in time, card drivers would likely hang too. */
        NOT_RESPONDING,
        /** PC/SC can't be used, e.g. its library or service is missing, so cards are not known. */
        UNKNOWN
    }

    private static FutureTask<State> pendingCheck;
    private static long pendingCheckStartNanos;

    private CardReaders() {
    }

    /**
     * Checks for an inserted card, waits for PC/SC at most the given time.
     */
    public static State check(long timeoutMillis) {
        var timeoutNanos = TimeUnit.MILLISECONDS.toNanos(timeoutMillis);
        FutureTask<State> check;
        synchronized (CardReaders.class) {
            // a check that didn't return in time is stuck in PC/SC, the service is still not responding
            if (pendingCheck != null && !pendingCheck.isDone() && System.nanoTime() - pendingCheckStartNanos > timeoutNanos)
                return State.NOT_RESPONDING;

            if (pendingCheck == null || pendingCheck.isDone()) {
                pendingCheck = new FutureTask<>(CardReaders::checkNow);
                pendingCheckStartNanos = System.nanoTime();
                var thread = new Thread(pendingCheck, "card-readers-check");
                thread.setDaemon(true); // a stuck PC/SC call must not block the exit
                thread.start();
            }
            check = pendingCheck;
        }

        try {
            return check.get(timeoutNanos, TimeUnit.NANOSECONDS);
        } catch (TimeoutException e) {
            Logging.log("Card readers not responding in " + timeoutMillis + " ms");
            return State.NOT_RESPONDING;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return State.UNKNOWN;
        } catch (ExecutionException e) {
            Logging.log("Unable to check card readers: " + e.getCause());
            return State.UNKNOWN;
        }
    }

    private static State checkNow() {
        try {
            // not the default factory, it would stay without PC/SC if the service wasn't running on the first call
            var terminals = TerminalFactory.getInstance("PC/SC", null).terminals();
            var withCard = terminals.list(CardTerminals.State.CARD_PRESENT);
            Logging.log("Card readers with card: " + withCard);
            return withCard.isEmpty() ? State.NO_CARD : State.CARD_PRESENT;
        } catch (Exception | LinkageError e) {
            // e.g. no PC/SC library on Linux, or the smart card service not running on Windows
            Logging.log("Unable to check card readers: " + e);
            return State.UNKNOWN;
        }
    }
}
