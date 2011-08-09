package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Empty extends Node {

    @Override
    public void printCSS(CssWriter out) {
        // do nothing
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Empty");
    }
}
