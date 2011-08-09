package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Keyword extends Node {
    private String _value;

    public Keyword(String value) {
        _value = value;
    }

    @Override
    public String getValue() {
        return _value;
    }

    @Override
    public void printCSS(CssWriter out) {
        out.print(_value);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Keyword", _value);
    }
}