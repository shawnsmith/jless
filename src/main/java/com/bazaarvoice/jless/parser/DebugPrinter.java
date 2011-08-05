package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.tree.Node;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class DebugPrinter {

    private final String _name;
    private final List<?> _values;

    public DebugPrinter(String name, Object... values) {
        this(name, Arrays.asList(values));
    }

    public DebugPrinter(String name, List<?> values) {
        _name = name;
        _values = values;
    }

    public void print(Appendable out, int indent) throws IOException {
        indent(out, indent);
        out.append('[').append(_name);
        if (hasNode(_values)) {
            String sep = "\n";
            for (Object value : _values) {
                out.append(sep);
                Object debug = toDebugPrinter(value);
                if (debug instanceof DebugPrinter) {
                    ((DebugPrinter) debug).print(out, indent + 2);
                } else {
                    indent(out, indent + 2);
                    out.append(String.valueOf(debug));
                }
                sep = ",\n";
            }
        } else {
            String sep = " ";
            for (Object value : _values) {
                out.append(sep).append(String.valueOf(value));
                sep = ", ";
            }
        }
        out.append(']');
    }

    private void indent(Appendable out, int indent) throws IOException {
        for (int i = 0; i < indent; i++) {
            out.append(' ');
        }
    }

    private boolean hasNode(Iterable<?> values) {
        for (Object value : values) {
            if (value instanceof Node) {
                return true;
            }
            if (value instanceof Iterable && hasNode((Iterable<?>) value)) {
                return true;
            }
            if (value instanceof Map &&
                    (hasNode((((Map<?, ?>) values).keySet())) || hasNode((((Map<?, ?>) values).values())))) {
                return true;
            }
        }
        return false;
    }

    private Object toDebugPrinter(Object object) {
        if (object instanceof DebugPrinter) {
            return object;
        }
        if (object instanceof Node) {
            return ((Node) object).toDebugPrinter();
        }
        if (object instanceof List) {
            return new DebugPrinter("List", (List<?>) object);
        }
        if (object instanceof Map) {
            return new DebugPrinter("Map", new ArrayList<Object>(((Map<?,?>)object).entrySet()));
        }
        if (object instanceof Map.Entry) {
            Map.Entry<?, ?> entry = (Map.Entry<?, ?>) object;
            return new DebugPrinter("Pair", entry.getKey(), entry.getValue());
        }
        return object;
    }
}
