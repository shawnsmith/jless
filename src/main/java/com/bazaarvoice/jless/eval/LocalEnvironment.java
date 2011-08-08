package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

public class LocalEnvironment implements Environment {

    private final Environment _parent;
    private final Map<String, Node> _values;

    public LocalEnvironment(Environment parent, Map<String, Node> values) {
        _parent = parent;
        _values = values;
    }

    public Environment extend(Map<String, Node> values) {
        return new LocalEnvironment(this, values);
    }

    public Node lookup(String variable) {
        Node value = _values.get(variable);
        if (value != null) {
            return value;
        }
        return _parent.lookup(variable);
    }

    public Function<List<Node>, Node> getFunction(String name) {
        return _parent.getFunction(name);
    }
}
