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

package com.uber.sdk.android.core.auth;

import com.uber.sdk.core.auth.Scope;

import org.junit.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class ScopeTest {

    @Test
    public void testParseScopesWithZero_shouldReturnNothing() throws Exception {
        Set<Scope> scopes = Scope.parseScopes(0);

        assertThat(scopes).isEmpty();
    }

    @Test
    public void testParseScopesWithNegativeValue_shouldReturnNothing() throws Exception {
        Set<Scope> scopes = Scope.parseScopes(-32);

        assertThat(scopes).isEmpty();
    }

    @Test
    public void testParseScopesWithOneScope_shouldReturn() throws Exception {
        Set<Scope> scopes = Scope.parseScopes(Scope.HISTORY.getBitValue());

        assertThat(scopes).contains(Scope.HISTORY);
    }

    @Test
    public void testParseScopesWithMultipleGeneralScopes_shouldReturn() throws Exception {
        Set<Scope> scopes = Scope.parseScopes(Scope.HISTORY.getBitValue() | Scope.PROFILE.getBitValue());

        assertThat(scopes).contains(Scope.HISTORY, Scope.PROFILE);
    }

    @Test
    public void testParseScopesWithMixLevelScopes_shouldReturn() throws Exception {
        Set<Scope> scopes = Scope.parseScopes(
                Scope.HISTORY.getBitValue() | Scope.REQUEST.getBitValue() | Scope.PROFILE.getBitValue());

        assertThat(scopes).contains(Scope.HISTORY, Scope.REQUEST, Scope.PROFILE);
    }
}