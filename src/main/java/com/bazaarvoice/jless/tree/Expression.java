package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Expression extends Node {
    private final List<Node> _values;

    public Expression(Node value) {
        this(Collections.singletonList(value));
    }

    public Expression(List<Node> values) {
        _values = values;
    }

    @Override
    public Node eval(Environment env) {
        if (_values.size() > 0) {
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
    public String toCSS(Environment env) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < _values.size(); i++) {
            if (i > 0) {
                buf.append(' ');
            }
            buf.append(_values.get(i).toCSS(env));
        }
        return buf.toString();
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
