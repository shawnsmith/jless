package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Combinator extends Node {

    private final String _value;

    public Combinator(String value) {
        _value = toCombinator(value);
    }

    public String getValue() {
        return _value;
    }

    private String toCombinator(String value) {
        if (" ".equals(value)) return " ";
        if ("& ".equals(value)) return "& ";
        return value != null ? value.trim() : "";
    }

    @Override
    public String toCSS(Environment env) {
        if (_value == null) return "";
        if ("".equals(_value)) return "";
        if (" ".equals(_value)) return " ";
        if ("&".equals(_value)) return "";
        if ("& ".equals(_value)) return " ";
        if (":".equals(_value)) return " :";
        if ("::".equals(_value)) return "::";
        if ("+".equals(_value)) return env.isCompressionEnabled() ? "+" : " + ";
        if ("~".equals(_value)) return env.isCompressionEnabled() ? "~" : " ~ ";
        if (">".equals(_value)) return env.isCompressionEnabled() ? ">" : " > ";
        throw new UnsupportedOperationException(_value);
    }

    @Override
    public String toString() {
        return _value;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Combinator", "'" + _value + "'");
    }
}
