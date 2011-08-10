package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.List;

public abstract class Node {

    public Node eval(Environment env) {
        return this;
    }

    public void flatten(List<Selector> contexts, List<Node> parentBlock, List<Node> globalBlock) {
        // do nothing by default.  subclasses override.
    }

    public abstract void printCss(CssWriter out);

    public String getStringValue() {
        return toString();
    }

    @Override
    public String toString() {
        CssWriter out = new CssWriter();
        out.print(this);
        return out.toString();
    }

    public abstract DebugPrinter toDebugPrinter();

    public Color toColor() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    public Node operate(char op, Node operand) {
        throw new UnsupportedOperationException(getClass().getName());
    }

    public static double operate(char op, double a, double b) {
        switch (op) {
            case '+': return a + b;
            case '-': return a - b;
            case '*': return a * b;
            case '/': return a / b;
            default: throw new IllegalStateException();
        }
    }
}
