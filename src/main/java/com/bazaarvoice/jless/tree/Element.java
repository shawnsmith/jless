package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
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

    public String getValue() {
        return _value;
    }

    @Override
    public String toCSS(Environment env) {
        return _combinator.toCSS(env) + _value;
    }

    @Override
    public String toString() {
        return _combinator + _value;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Element", _combinator, _value);
    }
}
