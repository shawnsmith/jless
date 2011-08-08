package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Value extends Node {

    private final List<Node> _value;

    public Value(Node expression) {
        this(Collections.singletonList(expression));
    }

    public Value(List<Node> value) {
        _value = value;
    }

    @Override
    public Node eval(Environment env) {
        if (_value.size() > 1) {
            List<Node> results = new ArrayList<Node>(_value.size());
            for (Node value : _value) {
                results.add(value.eval(env));
            }
            return new Value(results);
        } else if (_value.size() == 1) {
            return _value.get(0).eval(env);
        } else {
            return this;
        }
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        for (int i = 0; i < _value.size(); i++) {
            if (i > 0) {
                out.print(',');
                if (!out.isCompressionEnabled()) {
                    out.print(' ');
                }
            }
            _value.get(i).printCSS(env, out);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(_value, ", ");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Value", _value);
    }
}
