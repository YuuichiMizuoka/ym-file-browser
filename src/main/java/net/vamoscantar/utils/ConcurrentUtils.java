package net.vamoscantar.utils;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static java.util.concurrent.CompletableFuture.supplyAsync;

public class ConcurrentUtils {

    private ConcurrentUtils() {

    }

    public static <T> T race(Supplier<T> first, Supplier<T> second) throws ExecutionException, InterruptedException {
        return (T) CompletableFuture.anyOf(supplyAsync(first), supplyAsync(second)).get();
    }

}
