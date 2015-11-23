/*
 * Copyright (c) 2015 Uber Technologies, Inc.
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

package com.uber.sdk.android.rides;

import android.support.annotation.NonNull;

import com.google.api.client.repackaged.com.google.common.base.Joiner;
import com.google.common.io.Files;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

/**
 * Adds utility methods common amongst multiple test classes.
 */
public final class TestUtils {

    /**
     * Reads the file out as a string, trimming the result.
     *
     * @param path The root path of the File. If unable to access will dig down to module level.
     */
    public static String readProjectResource(String path) throws IOException {
        File file = new File(path);
        if (!file.exists()) {
            // If not at full path, check module dir
            List<String> list = Arrays.asList(path.split(File.pathSeparator));
            file = new File(Joiner.on(File.pathSeparatorChar).join(list.subList(1, list.size())));
        }
        return Files.toString(file, StandardCharsets.UTF_8).trim();
    }

    /**
     * Reads a URI stored as a text resource file and then adds the user agent.
     *
     * @param path The root path of the File. If unable to access will dig down to module level.
     * @param userAgent The user-agent to be added as a query parameter to the resulting URI.
     */
    @NonNull
    public static String readUriResourceWithUserAgentParam(@NonNull String path, @NonNull String userAgent)
            throws IOException {
        String uri = readProjectResource(path);
        return uri.concat(String.format("&user-agent=%s", userAgent));
    }
}
