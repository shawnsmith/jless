package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Alpha extends Node {

    private final Node _value;  // Keyword | Variable

    public Alpha(Node value) {
        if (!(value instanceof Keyword || value instanceof Variable)) {
            throw new IllegalArgumentException(value.toString());
        }
        _value = value;
    }

    @Override
    public Node eval(Environment env) {
        return new Alpha(_value.eval(env));
    }

    @Override
    public String toCSS(Environment env) {
        return "alpha(opacity=" + _value.toCSS(env) + ")";
    }

    @Override
    public String toString() {
        return "alpha(opacity=" + _value + ")";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Alpha", _value);
    }
}
