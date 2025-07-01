package cl.tenpo.learning.reactive.utils;

import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.reactivestreams.Subscriber;

import java.time.Duration;

@Slf4j
public class ModuleUtils {
    private static final Faker faker = new Faker();

    public static Faker faker() {
        return faker;
    }

    public static <T> Subscriber<T> subscriber() {
        return new DefaultSubscriber<>("");
    }

    public static <T> Subscriber<T> subscriber(String name) {
        return new DefaultSubscriber<>(name);
    }

    public static void sleep(Duration duration) {
        try {
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    public static void sleepSeconds(final long seconds) {
        try {
            Thread.sleep(seconds*1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
