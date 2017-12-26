/**
 * Copyright (c) 2017 EditorConfig Maven Plugin
 * project contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ec4j.maven.core;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditableResource extends Resource implements CharSequence {

    private static final Pattern EOL_MATCHER = Pattern.compile("$", Pattern.MULTILINE);

    private int hashCodeLoaded;

    StringBuilder text;

    public EditableResource(Path absPath, Path relPath, Charset encoding) {
        super(absPath, relPath, encoding);
    }

    /**
     * Primarily testing only.
     *
     * @param absPath
     * @param encoding
     * @param text
     */
    public EditableResource(Path absPath, Path relPath, Charset encoding, String text) {
        super(absPath, relPath, encoding);
        this.text = new StringBuilder(text);
        this.hashCodeLoaded = text.hashCode();
    }

    public String asString() {
        ensureReadSilent();
        return text.toString();
    }

    public boolean changed() {
        final int len = text.length();
        int hash = 0;
        for (int i = 0; i < len; i++) {
            // h = 31 * h + val[i];
            hash = 31 * hash + text.charAt(i);
        }
        return hash != this.hashCodeLoaded;
    }

    @Override
    public char charAt(int index) {
        ensureReadSilent();
        return text.charAt(index);
    }

    public void delete(int start, int end) {
        ensureReadSilent();
        text.delete(start, end);
    }

    private void ensureRead() throws IOException {
        if (text == null) {
            Reader r = null;
            try {
                r = super.openReader();
                int hash = 0;
                StringBuilder sb = new StringBuilder(256);
                char[] cbuf = new char[1024];
                int len;
                while ((len = r.read(cbuf)) >= 0) {
                    sb.append(cbuf, 0, len);
                    for (int i = 0; i < len; i++) {
                        hash = 31 * hash + cbuf[i];
                    }
                }
                this.text = sb;
                this.hashCodeLoaded = hash;
            } finally {
                if (r != null) {
                    r.close();
                }
            }
        }
    }

    private void ensureReadSilent() {
        try {
            ensureRead();
        } catch (IOException e) {
            throw new FormatException("Could not read " + absPath, e);
        }
    }

    public int findLineStart(int lineNumber) {
        ensureReadSilent();
        if (lineNumber == 1) {
            return 0;
        } else {
            int currentLine = 2;
            Matcher m = EOL_MATCHER.matcher(text);
            while (m.find()) {
                if (currentLine == lineNumber) {
                    int end = m.end();
                    final int len = text.length();
                    if (end < len) {
                        switch (text.charAt(end)) {
                        case '\n':
                            return end + 1;
                        case '\r':
                            end++;
                            if (end < len && text.charAt(end) == '\n') {
                                return end + 1;
                            } else {
                                return end;
                            }
                        }
                    }
                    return end;
                }
                if (currentLine > lineNumber) {
                    throw new IndexOutOfBoundsException("No such line " + lineNumber);
                }
                currentLine++;
            }
            throw new IndexOutOfBoundsException("No such line " + lineNumber);
        }
    }

    public void insert(int offset, CharSequence s) {
        ensureReadSilent();
        text.insert(offset, s);
    }

    @Override
    public int length() {
        ensureReadSilent();
        return text.length();
    }

    @Override
    public Reader openReader() throws IOException {
        ensureRead();
        return LineReader.of(text);
    }

    public void replace(int start, int end, String str) {
        ensureReadSilent();
        text.replace(start, end, str);
    }

    public void store() throws IOException {
        Writer w = null;
        try {
            w = Files.newBufferedWriter(absPath, encoding);
            char[] cbuf = new char[1024];
            int len = text.length();
            int i = 0;
            while (i < len) {
                final int srcEnd = Math.min(len, i + cbuf.length);
                text.getChars(i, srcEnd, cbuf, 0);
                w.write(cbuf, 0, srcEnd - i);
                i += srcEnd - i;
            }
        } finally {
            if (w != null) {
                w.close();
            }
        }

    }

    @Override
    public CharSequence subSequence(int start, int end) {
        ensureReadSilent();
        return text.subSequence(start, end);
    }

}
