package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Expression extends Node {
    private final List<Node> _values;

    public Expression(List<Node> values) {
        _values = values;
    }

    @Override
    public Node eval(Environment env) {
        if (_values.size() > 1) {
            List<Node> results = new ArrayList<Node>(_values.size());
            for (Node value : _values) {
                results.add(value.eval(env));
            }
            return new Expression(results);
        } else if (_values.size() == 1) {
            return _values.get(0).eval(env);
        } else {
            return this;
        }
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        for (int i = 0; i < _values.size(); i++) {
            if (i > 0) {
                out.print(' ');
            }
            _values.get(i).printCSS(env, out);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(_values, ' ');
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Expression", _values);
    }
}
