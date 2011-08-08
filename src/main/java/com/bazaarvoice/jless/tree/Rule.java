package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.parboiled.support.Position;

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

    @Override
    public Node eval(Environment env) {
        return new Rule(getPosition(), _name, _value.eval(env), _important);
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        if (_name instanceof Variable) {
            return;
        }
        out.indent(this);
        _name.printCSS(env, out);
        out.print(':');
        if (!out.isCompressionEnabled()) {
            out.print(' ');
        }
        _value.printCSS(env, out);
        if (_important) {
            out.print(" !important");
        }
        out.print(';');
        out.newline();
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
