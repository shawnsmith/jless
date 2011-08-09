package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Shorthand extends Node {

    private final Node _a, _b;

    public Shorthand(Node a, Node b) {
        _a = a;
        _b = b;
    }

    @Override
    public Node eval(Environment env) {
        return new Shorthand(_a.eval(env), _b.eval(env));
    }

    @Override
    public void printCSS(CssWriter out) {
        out.print(_a);
        out.print('/');
        out.print(_b);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Shorthand", _a, _b);
    }
}
