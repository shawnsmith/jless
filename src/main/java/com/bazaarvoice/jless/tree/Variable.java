package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.VariableException;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Variable extends Node {

    private final String _name;

    public Variable(String name) {
        _name = name;
    }

    @Override
    public Node eval(Environment env) {
        String name = _name;
        if (name.startsWith("@@")) {
            name = new Variable(name.substring(1)).eval(env).toString();
        }
        Node value = env.lookup(name);
        if (value == null) {
            throw new VariableException("Variable is undefined: " + name);
        }
        return value;
    }

    @Override
    public String toCSS(Environment env) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return _name;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Variable", _name);
    }
}
