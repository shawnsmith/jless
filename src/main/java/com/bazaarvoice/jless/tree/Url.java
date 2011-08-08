package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Url extends Node {

    private final Node _src;

    public Url(Node src) {
        if (!(src instanceof Anonymous || src instanceof DataUri || src instanceof Quoted || src instanceof Variable)) {
            throw new IllegalArgumentException(src.toString());
        }
        _src = src;
    }

    @Override
    public Node eval(Environment env) {
        return new Url(_src.eval(env));
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        out.print("url(");
        _src.printCSS(env, out);
        out.print(")");
    }

    @Override
    public String toString() {
        return "url(" + _src + ")";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Url", _src);
    }
}
