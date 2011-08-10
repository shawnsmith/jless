package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Anonymous extends Node {

    private final String _string;

    public Anonymous(String string) {
        _string = string;
    }

    @Override
    public void printCss(CssWriter out) {
        out.print(_string);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Anonymous", _string);
    }
}
