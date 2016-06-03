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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

public class PreconditionsTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testCheckStateTrue_shouldReturn() {
        Preconditions.checkState(true, "TestError");
    }


    @Test
    public void testCheckStateFalse_shouldThrow() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("TestError");

        Preconditions.checkState(false, "TestError");
    }


    @Test
    public void testCheckNotNullWithNotNull_shouldReturn() {
        final Object object = new Object();

        assertThat(Preconditions.checkNotNull(object, "TestError")).isSameAs(object);
    }

    @Test
    public void testCheckNotNullWithNull_shouldThrow() {
        exception.expect(NullPointerException.class);
        exception.expectMessage("TestError");

        Preconditions.checkNotNull(null, "TestError");
    }

    @Test
    public void testCheckNotEmptyWithNotEmpty_shouldReturn() {
        final Collection<String> collection = Arrays.asList("value1", "value2");

        assertThat(Preconditions.checkNotEmpty(collection, "TestError")).isSameAs(collection);
    }

    @Test
    public void testCheckNotEmptyWithEmpty_shouldThrow() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("TestError");

        final Collection<String> collection = Collections.EMPTY_LIST;

        assertThat(Preconditions.checkNotEmpty(collection, "TestError"));
    }

    @Test
    public void testCheckNotEmptyWithNulkl_shouldThrow() {
        exception.expect(IllegalStateException.class);
        exception.expectMessage("TestError");

        assertThat(Preconditions.checkNotEmpty(null, "TestError"));
    }
}