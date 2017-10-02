package org.l2x6.editorconfig.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.l2x6.editorconfig.core.EditorConfigService.OptionMap;
import org.l2x6.editorconfig.core.EditorConfigService.WellKnownKey;
import org.l2x6.editorconfig.format.Delete;

public class TextValidator implements Validator {

    private static final Pattern TRAILING_WHITESPACE_PATTERN = Pattern.compile("[ \t]+$");

    @Override
    public boolean canParse(Path path) {
        return true;
    }

    @Override
    public void process(Resource resource, OptionMap options, ViolationHandler violationHandler) throws IOException {
        try (BufferedReader in = new BufferedReader(resource.openReader())) {
            String line = null;
            int lineNumber = 1;
            while ((line = in.readLine()) != null) {
                Boolean trimTrailingWs = options.get(WellKnownKey.trim_trailing_whitespace);
                if (trimTrailingWs != null && trimTrailingWs.booleanValue()) {
                    Matcher m = TRAILING_WHITESPACE_PATTERN.matcher(line);
                    if (m.matches()) {
                        int start = m.start();
                        Violation violation = new Violation(resource, new Location(lineNumber, start+1), new Delete(m.end() - start));
                        violationHandler.handle(violation);
                    }
                }

            }
        }
    }


}
