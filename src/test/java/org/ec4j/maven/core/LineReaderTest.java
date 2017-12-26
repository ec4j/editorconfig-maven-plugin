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
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.ec4j.maven.core.LineReader.DelegatingLineReader;
import org.ec4j.maven.core.LineReader.StringBuilderReader;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(Parameterized.class)
public class LineReaderTest {

    private enum Doc {
        cr(new String[] { //
                "line 1\r", //
                "line 2\r" //
        }), //
        cr_(new String[] { //
                "line 1\r", //
                "line 2" //
        }), //
        crcr(new String[] { //
                "line 1\r", //
                "\r" //
        }), //
        crlf(new String[] { //
                "line 1\r\n", //
                "line 2\r\n" //
        }), //
        crlf_(new String[] { //
                "line 1\r\n", //
                "line 2" //
        }), //
        crlfcrlf(new String[] { //
                "line 1\r\n", //
                "\r\n" //
        }), //
        lf(new String[] { //
                "line 1\n", //
                "line 2\n" //
        }), lf_(new String[] { //
                "line 1\n", //
                "line 2" //
        }), lflf(new String[] { //
                "line 1\n", //
                "\n" //
        });
        private final String[] lines;

        Doc(String[] lines) {
            this.lines = lines;
        }

        public String[] getLines() {
            return lines;
        }

        public String getText() {
            StringBuilder sb = new StringBuilder();
            for (String line : lines) {
                sb.append(line);
            }
            return sb.toString();
        }

    }

    interface LineReaderFactory {
        LineReader create(String text);
    }

    @Parameters(name = "{index}: {0},{1}")
    public static Iterable<Object[]> data() {

        final LineReaderFactory[] factories = new LineReaderFactory[] { new LineReaderFactory() {
            @Override
            public LineReader create(String text) {
                return LineReader.of(new StringReader(text));
            }

            @Override
            public String toString() {
                return DelegatingLineReader.class.getSimpleName();
            }

        }, new LineReaderFactory() {
            @Override
            public LineReader create(String text) {
                return LineReader.of(new StringBuilder(text));
            }

            @Override
            public String toString() {
                return StringBuilderReader.class.getSimpleName();
            }
        } };
        List<Object[]> result = new ArrayList<>();

        for (LineReaderFactory f : factories) {
            for (Doc doc : Doc.values()) {
                result.add(new Object[] { doc, f });
            }
        }

        return result;
    }

    private final Doc doc;
    private final LineReaderFactory factory;

    public LineReaderTest(Doc doc, LineReaderFactory factory) {
        super();
        this.doc = doc;
        this.factory = factory;
    }

    @Test
    public void read() throws IOException {
        String text = doc.getText();
        try (LineReader in = factory.create(text)) {
            for (int i = 0; i < text.length(); i++) {
                char expected = text.charAt(i);
                int actual = in.read();
                Assert.assertEquals("At position " + i + " in document " + doc, expected, actual);
            }
            Assert.assertEquals(-1, in.read());
        }
    }

    @Test
    public void readLine() throws IOException {
        try (LineReader in = factory.create(doc.getText())) {
            int i = 1;
            for (String expected : doc.getLines()) {
                String actual = in.readLine();
                Assert.assertEquals("At line " + i + " in document " + doc, expected, actual);
                i++;
            }
            Assert.assertNull(in.readLine());
        }
    }

}
