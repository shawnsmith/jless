package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.OperationException;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Operation extends Node {

    private final char _op;
    private final Node _left, _right;

    public Operation(char op, Node left, Node right) {
        _op = op;
        _left = left;
        _right = right;
    }

    @Override
    public Node eval(Environment env) {
        Node a = _left.eval(env);
        Node b = _right.eval(env);

        if (a instanceof Dimension && b instanceof Color) {
            if (_op == '*' || _op == '+') {
                Node temp = b;
                b = a;
                a = temp;
            } else {
                throw new OperationException("Can't substract or divide a color from a number");
            }
        }

        return a.operate(_op, b);
    }

    @Override
    public void printCss(CssWriter out) {
        out.print(_left);
        out.print(' ');
        out.print(_op);
        out.print(' ');
        out.print(_right);
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Operation", _op, _left, _right);
    }
}