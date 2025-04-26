package digital.slovensko.autogram.core;

public interface BatchStartCallback {
    void accept(SigningKey key);

    void cancel();
}
