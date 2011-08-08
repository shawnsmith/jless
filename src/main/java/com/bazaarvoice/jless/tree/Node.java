package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public abstract class Node {

    public Node eval(Environment env) {
        return this;
    }

    public void printCSS(Environment env, CssWriter out) {
        out.print(toString());
    }

    public abstract DebugPrinter toDebugPrinter();

    public Color toColor() {
        throw new UnsupportedOperationException(getClass().getName());
    }

    public Node operate(char op, Node operand) {
        throw new UnsupportedOperationException();
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
