package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Comment extends Node {

    private final String _value;
    private final boolean _silent;

    public Comment(String value, boolean silent) {
        _value = value;
        _silent = silent;
    }

    public boolean isSilent() {
        return _silent;
    }

    @Override
    public String toCSS(Environment env) {
        return env.isCompressionEnabled() ? "" : _value;
    }

    @Override
    public String toString() {
        return _value;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Comment", _value, _silent);
    }
}