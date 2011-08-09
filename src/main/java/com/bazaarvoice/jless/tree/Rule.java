package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Rule extends NodeWithPosition {

    private final Node _name;
    private final Value _value;
    private final boolean _important;

    public Rule(int position, Node name, Node value, boolean important) {
        super(position);
        if (!(name instanceof Keyword || name instanceof Variable)) {
            throw new IllegalArgumentException(name.toString());
        }
        _name = name;
        _value = (value instanceof Value) ? (Value) value : new Value(value);
        _important = important;
    }

    public Node getName() {
        return _name;
    }

    @Override
    public Value getValue() {
        return _value;
    }

    @Override
    public Node eval(Environment env) {
        if (_name instanceof Variable) {
            return null;
        }
        return new Rule(getPosition(), _name, _value.eval(env), _important);
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        out.print(_name);
        out.print(':');
        if (!out.isCompressionEnabled()) {
            out.print(' ');
        }
        out.print(_value);
        if (_important) {
            out.print(" !important");
        }
        out.print(';');
        out.newline();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Rule", _name, _value, _important);
    }
}
