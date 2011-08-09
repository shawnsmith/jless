package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Combinator extends Node {

    private final String _value;

    public Combinator(String value) {
        _value = toCombinator(value);
    }

    @Override
    public String getValue() {
        return _value;
    }

    private String toCombinator(String value) {
        if (" ".equals(value)) return " ";
        if ("& ".equals(value)) return "& ";
        return value != null ? value.trim() : "";
    }

    @Override
    public void printCSS(CssWriter out) {
        if (_value == null) /* do nothing */;
        else if ("".equals(_value)) /* do nothing */;
        else if (" ".equals(_value)) out.print(' ');
        else if ("&".equals(_value)) /* do nothing */;
        else if ("& ".equals(_value)) out.print(' ');
        else if (":".equals(_value)) out.print(" :");
        else if ("::".equals(_value)) out.print("::");
        else if ("+".equals(_value)) out.print(out.isCompressionEnabled() ? "+" : " + ");
        else if ("~".equals(_value)) out.print(out.isCompressionEnabled() ? "~" : " ~ ");
        else if (">".equals(_value)) out.print(out.isCompressionEnabled() ? ">" : " > ");
        else throw new UnsupportedOperationException(_value);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Combinator", "'" + _value + "'");
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Combinator && _value.equals(((Combinator) obj)._value));
    }

    @Override
    public int hashCode() {
        return _value.hashCode() + 22783;
    }
}
