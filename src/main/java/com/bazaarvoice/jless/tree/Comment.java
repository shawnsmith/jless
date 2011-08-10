package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Comment extends Node {

    private final String _value;
    private final boolean _silent;

    public Comment(String value, boolean silent) {
        _value = value;
        _silent = silent;
    }

    @Override
    public String getStringValue() {
        return _value;
    }

    public boolean isSilent() {
        return _silent;
    }

    @Override
    public void printCSS(CssWriter out) {
        if (!out.isCompressionEnabled()) {
            out.print(_value);
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Comment", _value, _silent);
    }
}