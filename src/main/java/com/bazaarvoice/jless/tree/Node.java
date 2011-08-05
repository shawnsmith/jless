package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.parboiled.support.Position;

import java.util.List;

public abstract class Node {

    private Position _position;
    private List<Node> _trailingIgnorables;

    public Node eval(Environment env) {
        return this;
    }

    public String toCSS(Environment env) {
        return toString();
    }

    public abstract DebugPrinter toDebugPrinter();

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
