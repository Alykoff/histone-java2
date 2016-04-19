/*
 * Copyright (c) 2016 MegaFon
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.histone.v2.utils;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 *
 * @author Gali Alykoff
 */
public class AsyncUtils {
    public static <T> CompletableFuture<Optional<T>> sequenceOptional(Optional<CompletableFuture<T>> optionalFutures) {
        if (optionalFutures.isPresent()) {
            return optionalFutures.get().thenApply(Optional::ofNullable);
        } else {
            return CompletableFuture.completedFuture(Optional.empty());
        }
    }

    public static <T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.<T>toList())
        );
    }
    public static <T> CompletableFuture<LinkedList<T>> sequence(LinkedList<CompletableFuture<T>> futures) {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                new LinkedList<>(
                   futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.<T>toList())
                )
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
