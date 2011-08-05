package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Anonymous extends Node {

    private final String _string;

    public Anonymous(String string) {
        _string = string;
    }

    @Override
    public String toCSS(Environment env) {
        return _string;
    }

    @Override
    public String toString() {
        return _string;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Anonymous", _string);
    }
}
