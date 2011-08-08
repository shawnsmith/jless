package com.bazaarvoice.jless.tree;

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
    public String toString() {
        return _value;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Keyword", _value);
    }
}
