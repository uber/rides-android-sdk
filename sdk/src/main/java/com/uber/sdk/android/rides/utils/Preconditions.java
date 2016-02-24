package com.uber.sdk.android.rides.utils;

import android.support.annotation.Nullable;

/**
 * Methods that help to check whether method or constructor invoked properly.
 * <p/>
 * Created by Antonenko Viacheslav on 24/02/16.
 */
public final class Preconditions {

    private Preconditions() {
        throw new AssertionError();
    }


    /**
     * @param expression a boolean expression
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void checkState(boolean expression) {
        if (!expression) {
            throw new IllegalStateException();
        }
    }

    /**
     * @param expression   a boolean expression
     * @param errorMessage the exception message to use if the check fails; will be converted to a
     *                     string using {@link String#valueOf(Object)}
     * @throws IllegalStateException if {@code expression} is false
     */
    public static void checkState(boolean expression, @Nullable Object errorMessage) {
        if (!expression) {
            throw new IllegalStateException(String.valueOf(errorMessage));
        }
    }
}
