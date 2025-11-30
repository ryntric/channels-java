package io.github.ryntric.util;


@FunctionalInterface
public interface ThrowableSupplier<T, E extends Throwable> {

    T get() throws E, IllegalAccessException;

    static <T, E extends Throwable> T sneaky(ThrowableSupplier<T, E> supplier) {
        try {
            return supplier.get();
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }
    }
}
