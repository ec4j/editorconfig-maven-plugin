package org.l2x6.editorconfig.core;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.l2x6.editorconfig.core.LineReader.DelegatingLineReader;
import org.l2x6.editorconfig.core.LineReader.StringBuilderReader;

@RunWith(Parameterized.class)
public class LineReaderTest {

    interface LineReaderFactory {
        LineReader create(String text);
    }

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
        lf(new String[] { //
                "line 1\n", //
                "line 2\n" //
        }), //
        lf_(new String[] { //
                "line 1\n", //
                "line 2" //
        }), //
        lflf(new String[] { //
                "line 1\n", //
                "\n" //
        }), //
        crlfcrlf(new String[] { //
                "line 1\r\n", //
                "\r\n" //
        }),
        crlf_(new String[] { //
                "line 1\r\n", //
                "line 2" //
        }),
        crlf(new String[] { //
                "line 1\r\n", //
                "line 2\r\n" //
        });
        private final String[] lines;

        private Doc(String[] lines) {
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

    private final LineReaderFactory factory;
    private final Doc doc;

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
