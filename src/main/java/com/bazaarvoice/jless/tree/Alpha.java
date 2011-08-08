package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
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
    public void printCSS(Environment env, CssWriter out) {
        out.print("alpha(opacity=");
        _value.printCSS(env, out);
        out.print(")");
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
