package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.LinkedHashMap;
import java.util.Map;

public class Progid extends Node {

    private final String _name;
    private final Map<String, Node> _valueMap;

    public Progid(String name, Map<String, Node> valueMap) {
        _name = name;
        _valueMap = valueMap;
    }

    @Override
    public Node eval(Environment env) {
        if (_valueMap.size() > 0) {
            Map<String, Node> values = new LinkedHashMap<String, Node>();
            for (Map.Entry<String, Node> entry : _valueMap.entrySet()) {
                values.put(entry.getKey(), entry.getValue().eval(env));
            }
            return new Progid(_name, values);
        } else {
            return this;
        }
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        out.print(_name);
        out.print('(');
        boolean first = true;
        for (Map.Entry<String, Node> entry : _valueMap.entrySet()) {
            if (!first) {
                out.print(',');
            }
            first = false;
            out.print(entry.getKey());
            out.print('=');
            entry.getValue().printCSS(env, out);
        }
        out.print(')');
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        buf.append(_name);
        buf.append('(');
        boolean first = true;
        for (Map.Entry<String, Node> entry : _valueMap.entrySet()) {
            if (!first) {
                buf.append(", ");
            }
            first = false;
            buf.append(entry.getKey());
            buf.append('=');
            buf.append(entry.getValue());
        }
        buf.append(')');
        return buf.toString();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Progid", _name, _valueMap);
    }
}
