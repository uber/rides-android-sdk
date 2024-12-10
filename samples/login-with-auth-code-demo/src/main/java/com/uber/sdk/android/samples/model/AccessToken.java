/*
 * Copyright (c) 2023 Uber Technologies, Inc.
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

package com.uber.sdk.android.samples.model;

import java.util.Objects;

public class AccessToken {
    private final long expires_in;
    private final String scopes;
    private final String access_token;
    private final String refresh_token;
    private final String token_type;

    /**
     * @param expiresIn    the time that the access token expires.
     * @param scopes       space delimited list of Scopes.
     * @param token        the Uber API access token.
     * @param refreshToken the Uber API refresh token.
     * @param tokenType    the Uber API token type.
     */
    public AccessToken(
            long expiresIn,
            String scopes,
            String token,
            String refreshToken,
            String tokenType) {
        this.expires_in = expiresIn;
        this.scopes = scopes;
        this.access_token = token;
        this.refresh_token = refreshToken;
        this.token_type = tokenType;
    }

    /**
     * Gets the raw token used to make API requests
     *
     * @return the raw token.
     */
    public String getToken() {
        return access_token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AccessToken that = (AccessToken) o;

        if (expires_in != that.expires_in) return false;
        if (!Objects.equals(scopes, that.scopes)) return false;
        if (!Objects.equals(access_token, that.access_token))
            return false;
        if (!Objects.equals(refresh_token, that.refresh_token))
            return false;
        return Objects.equals(token_type, that.token_type);

    }

    @Override
    public int hashCode() {
        int result = (int) (expires_in ^ (expires_in >>> 32));
        result = 31 * result + (scopes != null ? scopes.hashCode() : 0);
        result = 31 * result + (access_token != null ? access_token.hashCode() : 0);
        result = 31 * result + (refresh_token != null ? refresh_token.hashCode() : 0);
        result = 31 * result + (token_type != null ? token_type.hashCode() : 0);
        return result;
    }
}
