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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.CharBuffer;

/**
 * A {@link Reader} able to read line by line. The substantial difference against {@link BufferedReader} is that
 * {@link LineReader}'s lines do end with the actual the end of line characters.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public abstract class LineReader extends Reader {

    /**
     * A {@link LineReader} that reads from a {@link Reader} delegate.
     */
    static class DelegatingLineReader extends LineReader {
        private final Reader delegate;
        private int readAhead = -1;

        DelegatingLineReader(Reader delegate) {
            super();
            this.delegate = delegate;
        }

        @Override
        public void close() throws IOException {
            delegate.close();
        }

        @Override
        public void mark(int readAheadLimit) throws IOException {
            delegate.mark(readAheadLimit);
        }

        @Override
        public boolean markSupported() {
            return delegate.markSupported();
        }

        @Override
        public int read() throws IOException {
            if (readAhead >= 0) {
                int r = readAhead;
                readAhead = -1;
                return r;
            } else {
                return delegate.read();
            }
        }

        @Override
        public int read(char[] cbuf) throws IOException {
            int len = cbuf.length;
            if (len == 0) {
                return 0;
            } else if (readAhead >= 0 && len > 0) {
                cbuf[0] = (char) readAhead;
                readAhead = -1;
                int cnt = delegate.read(cbuf, 1, len - 1);
                return cnt + 1;
            } else {
                return delegate.read(cbuf);
            }
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            if (len == 0) {
                return 0;
            } else if (readAhead >= 0 && len > 0) {
                cbuf[0] = (char) readAhead;
                readAhead = -1;
                int cnt = delegate.read(cbuf, 1, len - 1);
                return cnt + 1;
            } else {
                return delegate.read(cbuf, off, len);
            }
        }

        @Override
        public int read(CharBuffer target) throws IOException {
            int len = target.remaining();
            if (len == 0) {
                return 0;
            } else {
                char[] cbuf = new char[len];
                if (readAhead >= 0 && len > 0) {
                    cbuf[0] = (char) readAhead;
                    readAhead = -1;
                    int cnt = delegate.read(cbuf, 1, len - 1);
                    target.put(cbuf, 0, cnt + 1);
                    return cnt + 1;
                } else {
                    int cnt = delegate.read(cbuf, 1, len);
                    if (cnt > 0) {
                        target.put(cbuf, 0, cnt);
                    }
                    return cnt;
                }
            }
        }

        @Override
        public String readLine() throws IOException {
            int ch = read();
            if (ch < 0) {
                return null;
            } else {
                StringBuilder sb = new StringBuilder(120);
                do {
                    switch (ch) {
                    case '\r':
                        sb.append((char) ch);
                        readAhead = read();
                        if (readAhead == '\n') {
                            sb.append('\n');
                            readAhead = -1;
                        }
                        return sb.toString();
                    case '\n':
                        sb.append((char) ch);
                        return sb.toString();
                    default:
                        sb.append((char) ch);
                        break;
                    }
                } while ((ch = read()) >= 0);
                return sb.toString();
            }
        }

        @Override
        public boolean ready() throws IOException {
            return readAhead >= 0 || delegate.ready();
        }

        @Override
        public void reset() throws IOException {
            delegate.reset();
        }

        @Override
        public long skip(long n) throws IOException {
            if (n == 0) {
                return 0;
            } else if (readAhead >= 0) {
                long cnt = delegate.skip(n);
                readAhead = -1;
                return cnt + 1;
            } else {
                return delegate.skip(n);
            }
        }
    }

    /**
     * A {@link LineReader} optimized for reading from a {@link StringBuilder}.
     */
    static class StringBuilderReader extends LineReader {

        private int mark = 0;

        private int offset = 0;

        private final StringBuilder text;

        StringBuilderReader(StringBuilder text) {
            this.text = text;
        }

        /** {@inheritDoc} */
        @Override
        public void close() {
            offset = 0;
            mark = 0;
        }

        /** {@inheritDoc} */
        @Override
        public void mark(int readAheadLimit) {
            mark = offset;
        }

        /**
         * @return {@code true}
         */
        @Override
        public boolean markSupported() {
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public int read() {
            if (offset >= text.length()) {
                return -1;
            } else {
                return text.charAt(offset++);
            }
        }

        /** {@inheritDoc} */
        @Override
        public int read(char[] cbuf, int off, int len) {
            if (offset >= text.length()) {
                return -1;
            }

            final int srcEnd = Math.min(text.length(), offset + len);
            text.getChars(offset, srcEnd, cbuf, off);
            int count = srcEnd - offset;
            offset += count;
            return count;
        }

        @Override
        public String readLine() throws IOException {
            final int len = text.length();
            if (offset >= len) {
                return null;
            }
            StringBuilder sb = new StringBuilder(120);
            while (offset < len) {
                char ch = text.charAt(offset++);
                switch (ch) {
                case '\r':
                    sb.append(ch);
                    if (offset < len && text.charAt(offset) == '\n') {
                        sb.append('\n');
                        offset++;
                    }
                    return sb.toString();
                case '\n':
                    sb.append(ch);
                    return sb.toString();
                default:
                    sb.append(ch);
                    break;
                }
            }
            return sb.toString();
        }

        /** {@inheritDoc} */
        @Override
        public void reset() {
            offset = mark;
        }

        /** {@inheritDoc} */
        @Override
        public long skip(long n) {
            if (n < 0) {
                throw new IllegalArgumentException("Number of characters to skip is less than zero: " + n);
            }
            if (offset >= text.length()) {
                return -1;
            }
            int dest = (int) Math.min(text.length(), offset + n);
            int count = dest - offset;
            offset = dest;
            return count;
        }

        /** @return {@link #text} */
        @Override
        public String toString() {
            return text.toString();
        }
    }

    /**
     * @param delegate
     *            the {@link Reader} to read from
     * @return a new {@link LineReader} that reads from the given {@link Reader}
     */
    public static LineReader of(Reader delegate) {
        return new DelegatingLineReader(delegate);
    }

    /**
     * @param text
     *            the {@link StringBuilder} to read from
     * @return a new {@link LineReader} optimized for reading from a {@link StringBuilder}.
     */
    public static LineReader of(StringBuilder text) {
        return new StringBuilderReader(text);
    }

    /**
     * @return a {@link String} containing the line incl. the end of line characters or {@code null} in case there are
     *         no more lines to read
     * @throws IOException
     *             on I/O problems
     */
    public abstract String readLine() throws IOException;
}
