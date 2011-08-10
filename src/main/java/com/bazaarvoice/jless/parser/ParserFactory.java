package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.Importer;
import org.parboiled.transform.ParserTransformer;

import java.lang.reflect.Constructor;

/**
 * Substitute for {@link org.parboiled.Parboiled#createParser(Class, Object...)} that might be marginally faster.
 */
public class ParserFactory {

    private static final Constructor<? extends Parser> _sConstructor = findConstructor();

    public static Parser newInstance() {
        return newInstance(null);
    }

    public static Parser newInstance(Importer importer) {
        try {
            return _sConstructor.newInstance(importer);
        } catch (Exception e) {
            throw new RuntimeException("Error creating extended parser class: " + e.getMessage(), e);
        }
    }

    private static Constructor<? extends Parser> findConstructor() {
        try {
            return ParserTransformer.transformParser(Parser.class).getConstructor(Importer.class);
        } catch (Exception e) {
            throw new RuntimeException("Error creating extended parser class: " + e.getMessage(), e);
        }
    }
}
