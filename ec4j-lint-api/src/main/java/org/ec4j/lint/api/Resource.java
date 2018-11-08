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
package org.ec4j.lint.api;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.charset.MalformedInputException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A readable resource. Consists of a file {@link Path} and a {@link Charset}.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
public class Resource {

    /**
     * A utility for line Number %lt;-&gt; file offset transformations.
     * @since 0.0.9
     */
    static class LineIndex {

        /**
         * A {@link LineIndex} builder.
         */
        static class Builder {
            private static final int[] EMPTY = new int[0];
            private int length = 0;
            private int[] lineStartOffsets = EMPTY;

            /**
             * @return a new {@link LineIndex}
             */
            public LineIndex build() {
                int[] lines = new int[length];
                System.arraycopy(lineStartOffsets, 0, lines, 0, length);
                lineStartOffsets = EMPTY;
                return new LineIndex(lines);
            }

            /**
             * Add a new line starting at the given offset.
             *
             * @param offset a zero based offset
             * @return this {@link Builder}
             */
            public Builder lineStartOffset(int offset) {
                if (length >= lineStartOffsets.length) {
                    final int[] newArr = new int[lineStartOffsets.length + 16];
                    System.arraycopy(lineStartOffsets, 0, newArr, 0, lineStartOffsets.length);
                    lineStartOffsets = newArr;
                }
                lineStartOffsets[length++] = offset;
                return this;
            }
        }

        /**
         * @return a new {@link Builder}
         */
        public static Builder builder() {
            return new Builder();
        }

        /**
         * @param text the {@link CharSequence} to index
         * @return a new {@link LineIndex} for the given {@code text}
         */
        public static LineIndex of(CharSequence text) {
            final Builder b = new Builder();
            final int len = text.length();
            Matcher m = EOL_MATCHER.matcher(text);
            while (m.find()) {
                int end = m.end();
                if (end < len) {
                    switch (text.charAt(end)) {
                    case '\n':
                        b.lineStartOffset(end + 1);
                        break;
                    case '\r':
                        end++;
                        if (end < len && text.charAt(end) == '\n') {
                            b.lineStartOffset(end + 1);
                            break;
                        } else {
                            b.lineStartOffset(end);
                            break;
                        }
                    }
                }
            }
            return b.build();
        }

        /**
         * First line start offset is always zero. The first element in {@link #lineStartOffsets} thus contains the
         * start offset of the second line.
         */
        final int[] lineStartOffsets;

        LineIndex(int[] lineStartOffsets) {
            this.lineStartOffsets = lineStartOffsets;
        }

        /**
         * @param lineNumber first line number is {@code 1}
         * @return the zero based offset of the given line
         */
        public int findLineStart(int lineNumber) {
            if (lineNumber == 1) {
                return 0;
            } else if (lineNumber < 1 && lineNumber - 2 >= lineStartOffsets.length) {
                throw new ArrayIndexOutOfBoundsException(String.format("Cannot access line %d, %s has only %d entries",
                        lineNumber, LineIndex.class.getName(), lineStartOffsets.length + 1));
            } else {
                return lineStartOffsets[lineNumber - 2];
            }
        }

        /**
         * @param offset the zero based index in {@link #text} to find the {@link Location} for
         * @return the {@link Location} at the given {@code offset}
         */
        public Location findLocation(int offset) {
            if (offset == 0) {
                return Location.initial();
            }
            final int len = lineStartOffsets.length;
            if (len == 0) {
                return new Location(1, offset + 1);
            }
            for (int i = 0; i < len; i++) {
                if (lineStartOffsets[i] == offset) {
                    return new Location(i + 2, 1);
                } else if (lineStartOffsets[i] > offset) {
                    if (i == 0) {
                        return new Location(1, offset + 1);
                    } else {
                        final int column = offset - lineStartOffsets[i - 1] + 1;
                        return new Location(i + 1, column);
                    }
                }
            }
            final int column = offset - lineStartOffsets[len - 1] + 1;
            return new Location(len + 1, column);
        }
    }

    private static final Pattern EOL_MATCHER = Pattern.compile("$", Pattern.MULTILINE);

    private final Path absPath;
    private final Charset encoding;
    /**
     * The hash code at load time. Can be used to decide if this {@link Resource} was changed since it was loaded.
     */
    private int hashCodeLoaded;

    private LineIndex lineIndex;

    private final Path relPath;

    /** The content of this {@link Resource} */
    StringBuilder text;

    /**
     * @param absPath the absolute path to the underlying file
     * @param relPath the path to the underlying file relative to the current projects root directory (used for
     *        reporting only)
     * @param encoding the {@link Charset} to use when reading from the underlying file
     */
    public Resource(Path absPath, Path relPath, Charset encoding) {
        super();
        this.absPath = absPath;
        this.relPath = relPath;
        this.encoding = encoding;
    }

    /**
     * Primarily for testing.
     *
     * @param absPath
     * @param relPath
     * @param encoding
     * @param text
     */
    public Resource(Path absPath, Path relPath, Charset encoding, String text) {
        this(absPath, relPath, encoding);
        this.text = new StringBuilder(text);
        this.hashCodeLoaded = text.hashCode();
    }

    /**
     * @return {@code true} if the {@link #text} was changed since it was loaded from the underlying file; {@code false}
     *         otherwise
     */
    public boolean changed() {
        final int len = text.length();
        int hash = 0;
        for (int i = 0; i < len; i++) {
            // h = 31 * h + val[i];
            hash = 31 * hash + text.charAt(i);
        }
        return hash != this.hashCodeLoaded;
    }

    /**
     * @param index the index of the <code>char</code> to be returned
     *
     * @return the <code>char</code> at the specified index in {@link #text}
     *
     * @throws IndexOutOfBoundsException if the <tt>index</tt> argument is negative or not less than <tt>length()</tt>
     */
    public char charAt(int index) {
        ensureReadSilent();
        return text.charAt(index);
    }

    /**
     * Deletes the given subsequence from {@link #text}
     *
     * @param start the start of the deletion (a zero based index, included)
     * @param end the end of the deletion (a zero based index, excluded)
     */
    public void delete(int start, int end) {
        ensureReadSilent();
        text.delete(start, end);
        invalidateIndex();
    }

    /**
     * Creates {@link #lineIndex} if ncessary.
     */
    private void ensureIndexAvailable() {
        if (this.lineIndex == null) {
            ensureReadSilent();
            this.lineIndex = LineIndex.of(text);
        }
    }

    /**
     * Read the content of the underlying file to {@link #text} if {@link #text} is still {@code null}.
     *
     * @throws IOException
     */
    private void ensureRead() throws IOException {
        if (text == null) {
            Reader r = null;
            try {
                r = Files.newBufferedReader(absPath, encoding);
                int hash = 0;
                StringBuilder sb = new StringBuilder(256);
                char[] cbuf = new char[8192];
                int len;
                while ((len = r.read(cbuf)) >= 0) {
                    sb.append(cbuf, 0, len);
                    for (int i = 0; i < len; i++) {
                        hash = 31 * hash + cbuf[i];
                    }
                }
                this.text = sb;
                invalidateIndex();
                this.hashCodeLoaded = hash;
            } catch (MalformedInputException e) {
                throw new FormatException("Could not read " + absPath
                        + ". This may mean that it is a binary file and you should exclude it from editorconfig processing.",
                        e);
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

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Resource other = (Resource) obj;
        if (encoding == null) {
            if (other.encoding != null)
                return false;
        } else if (!encoding.equals(other.encoding))
            return false;
        if (absPath == null) {
            if (other.absPath != null)
                return false;
        } else if (!absPath.equals(other.absPath))
            return false;
        return true;
    }

    /**
     * @param lineNumber the 1 based line number to find the offset for
     * @return the zero based character offset of the first character of the given line
     */
    public int findLineStart(int lineNumber) {
        ensureIndexAvailable();
        return lineIndex.findLineStart(lineNumber);
    }

    /**
     * @param offset the zero based index in {@link #text} to find the {@link Location} for
     * @return the {@link Location} at the given {@code offset}
     */
    public Location findLocation(int offset) {
        ensureReadSilent();
        if (offset == 0) {
            return Location.initial();
        }
        int line = 1;
        int column = 1;
        for (int i = 0; i < text.length() && i <= offset; i++) {
            if (i == offset) {
                return new Location(line, column);
            }
            char ch = text.charAt(i);
            switch (ch) {
            case '\r':
                if (i + 1 < text.length() && text.charAt(i + 1) == '\n') {
                    i++;
                    if (i == offset) {
                        return new Location(line, column + 1);
                    }
                }
                line++;
                column = 1;
                break;
            case '\n':
                line++;
                column = 1;
                break;
            default:
                column++;
                break;
            }
        }
        return new Location(line, column);
    }

    /**
     * @return the {@link Charset} to use when reading from this {@link Resource}
     */
    public Charset getEncoding() {
        return encoding;
    }

    /**
     * @return the absolute {@link Path} to the underlying file
     */
    public Path getPath() {
        return absPath;
    }

    /**
     * Ensures that the content was read into {@link #text} and return {@code text.toString()}
     *
     * @return {@link #text}
     */
    public String getText() {
        ensureReadSilent();
        return text.toString();
    }

    /**
     * @return the internal {@link #text} {@link StringBuilder}
     */
    public CharSequence getTextAsCharSequence() {
        ensureReadSilent();
        return text;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((encoding == null) ? 0 : encoding.hashCode());
        result = prime * result + ((absPath == null) ? 0 : absPath.hashCode());
        return result;
    }

    /**
     * Insert the given {@code string} at the given {@code offset}.
     *
     * @param offset a zero based character offset where to insert the {@code string}
     * @param string the string to insert
     */
    public void insert(int offset, CharSequence string) {
        ensureReadSilent();
        text.insert(offset, string);
        invalidateIndex();
    }

    /**
     * Sets {@link #lineIndex} to {@code null}
     */
    private void invalidateIndex() {
        this.lineIndex = null;
    }

    /**
     * @return the number of <code>char</code>s in the {@link #text}.
     */
    public int length() {
        ensureReadSilent();
        return text.length();
    }

    /**
     * @return a new Reader from the underlying file
     * @throws IOException on I/O problems
     */
    public Reader openReader() throws IOException {
        ensureRead();
        return LineReader.of(text);
    }

    /**
     * Replace the subsequence given by {@code start} and {@code end} by the given {@code replacement}.
     *
     * @param start the start of the subsequence to replace (a zero based index, included)
     * @param end the end of the subsequence to replace (a zero based index, excluded)
     * @param replacement the replacement
     */
    public void replace(int start, int end, String replacement) {
        ensureReadSilent();
        text.replace(start, end, replacement);
        invalidateIndex();
    }

    /**
     * Write {@link #text} back to the underlying file.
     *
     * @throws IOException
     */
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

    /**
     * @param start the start index, inclusive
     * @param end the end index, exclusive
     *
     * @return a subsequence in the internal {@link #text} {@link StringBuilder}
     *
     * @throws IndexOutOfBoundsException if <tt>start</tt> or <tt>end</tt> are negative, if <tt>end</tt> is greater than
     *         <tt>length()</tt>, or if <tt>start</tt> is greater than <tt>end</tt>
     */
    public CharSequence subSequence(int start, int end) {
        ensureReadSilent();
        return text.subSequence(start, end);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return relPath.toString();
    }

}
