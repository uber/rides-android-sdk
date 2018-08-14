/*
 * Copyright (c) 2016 Uber Technologies, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.uber.sdk.android.core.utils;

import android.support.annotation.NonNull;

import java.util.Collection;

/**
 * Simple static methods to be called at the start of your own methods to verify
 * correct arguments and state.
 */
public class Preconditions {

    /**
     * Ensures the truth of an expression involving the state of the calling
     * instance, but not involving any parameters to the calling method.
     *
     * @param expression a boolean expression
     * @param errorMessage error message thrown if expression is false.
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void checkState(final boolean expression, @NonNull String errorMessage) {
        if (!expression) {
            throw new IllegalStateException(errorMessage);
        }
    }

    /**
     * Ensures the value is not null.
     *
     * @param value value to be validated.
     * @param errorMessage error message thrown if value is null.
     * @param <T> type of value.
     * @return value passed in if not null.
     */
    @NonNull
    public static <T> T checkNotNull(T value, @NonNull String errorMessage) {
        if (value == null) {
            throw new NullPointerException(errorMessage);
        }
        return value;
    }

    /**
     * Ensures a collection is neither null nor empty.
     *
     * @param collection collection to be validated.
     * @param errorMessage error message to be thrown if collection is null or empty.
     * @param <T> type of the collection item.
     * @return collection passed in.
     */
    @NonNull
    public static <T> Collection<T> checkNotEmpty(Collection<T> collection, @NonNull String errorMessage) {
        checkState(collection != null && !collection.isEmpty(), errorMessage);
        return collection;
    }
}
