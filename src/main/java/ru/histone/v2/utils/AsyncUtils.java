package ru.histone.v2.utils;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * Created by gali.alykoff on 27/01/16.
 */
public class AsyncUtils {
    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.<T>toList())
        );
    }

    public static <T> CompletableFuture<List<T>> sequence(CompletableFuture<T>... futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures);

        return allDoneFuture.thenApply(v -> Arrays.asList(futures).stream()
                .map(CompletableFuture::join)
                .collect(Collectors.<T>toList())
        );
    }
}
