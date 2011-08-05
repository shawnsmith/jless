package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Rule extends Node {

    private final Node _name;
    private final Value _value;
    private final boolean _important;

    public Rule(Node name, Node value, boolean important) {
        if (!(name instanceof Keyword || name instanceof Variable)) {
            throw new IllegalArgumentException(name.toString());
        }
        _name = name;
        _value = (value instanceof Value) ? (Value) value : new Value(value);
        _important = important;
    }

    @Override
    public Node eval(Environment env) {
        return new Rule(_name, _value.eval(env), _important);
    }

    @Override
    public String toCSS(Environment env) {
        if (_name instanceof Variable) {
            return "";
        }
        StringBuilder buf = new StringBuilder();
        buf.append(_name);
        buf.append(':');
        if (!env.isCompressionEnabled()) {
            buf.append(' ');
        }
        buf.append(_value.toCSS(env));
        if (_important) {
            buf.append(" !important");
        }
        buf.append(';');
        return buf.toString();
    }

    @Override
    public String toString() {
        return _name + ": " + _value + (_important ? " !important" : "") + ';';
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Rule", _name, _value, _important);
    }
}
