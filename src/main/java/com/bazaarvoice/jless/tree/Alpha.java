package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Alpha extends Node {

    private final Node _value;  // Keyword | Variable

    public Alpha(Node value) {
        _value = value;
    }

    @Override
    public String getStringValue() {
        return _value.getStringValue();
    }

    @Override
    public Node eval(Environment env) {
        return new Alpha(_value.eval(env));
    }

    @Override
    public void printCss(CssWriter out) {
        out.print("alpha(opacity=");
        out.print(_value);
        out.print(")");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Alpha", _value);
    }
}
