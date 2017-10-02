package org.l2x6.editorconfig.core;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

public class Validators {
    /**
     * @param path
     * @param suffixes file name extensions including period, such as {@code ".txt"} or {@code ".xml"}
     * @return
     */
    public static boolean hasAnyOfExtensions(Path path, Collection<String> suffixes) {
        final String lastSegment = path.getFileName().toString().toLowerCase();
        for (String suffix : suffixes) {
            if (lastSegment.endsWith(suffix)) {
                return true;
            }
        }
        return false;
    }

    private final List<Validator> parsers;
    private final Validator defaultParser;

    public Validators(List<Validator> parsers, Validator defaultParser) {
        super();
        this.parsers = parsers;
        this.defaultParser = defaultParser;
    }

    public static Validators scan(ClassLoader classLoader) {
        List<Validator> parsers = new ArrayList<>();
        final ServiceLoader<Validator> loader = java.util.ServiceLoader.load(Validator.class, classLoader);
        final Iterator<Validator> it = loader.iterator();
        while (it.hasNext()) {
            Validator parser = it.next();
            parsers.add(parser);
        }
        return new Validators(Collections.unmodifiableList(parsers), new TextValidator());
    }

    public Validator findParser(Path path) {
        for (Validator p : parsers) {
            if (p.canParse(path)) {
                return p;
            }
        }
        return defaultParser;
    }
}
