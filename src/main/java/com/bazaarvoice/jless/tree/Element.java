package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Element extends Node {

    private final Combinator _combinator;
    private final String _value;

    public Element(String combinator, String value) {
        this(new Combinator(combinator), value);
    }

    public Element(Combinator combinator, String value) {
        _combinator = combinator;
        _value = value != null ? value.trim() : "";
    }

    public Combinator getCombinator() {
        return _combinator;
    }

    @Override
    public String getStringValue() {
        return _value;
    }

    @Override
    public void printCss(CssWriter out) {
        out.print(_combinator);
        out.print(_value);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Element", _combinator, _value);
    }


    @Override
    public boolean equals(Object obj) {
        return obj == this || (obj instanceof Element &&
                _value.equals(((Element) obj)._value) &&
                _combinator.equals(((Element) obj)._combinator));
    }

    @Override
    public int hashCode() {
        return _combinator.hashCode() * 13 + _value.hashCode();
    }
}
