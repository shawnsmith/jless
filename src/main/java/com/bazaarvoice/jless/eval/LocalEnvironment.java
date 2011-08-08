package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalEnvironment implements Environment {

    private final Environment _parent;
    private final Map<String, Supplier<Node>> _variableMap;

    public LocalEnvironment(Environment parent, Map<String, Node> variableMap) {
        _parent = parent;

        // variables are evaluated lazily in their own context.  basically this implements lexical scoping.
        _variableMap = new HashMap<String, Supplier<Node>>();
        final Environment self = this;
        for (Map.Entry<String, Node> entry : variableMap.entrySet()) {
            String key = entry.getKey();
            final Node value = entry.getValue();
            _variableMap.put(key, Suppliers.memoize(new Supplier<Node>() {
                public Node get() {
                    return value.eval(self);
                }
            }));
        }
    }

    public Environment extend(Map<String, Node> values) {
        return new LocalEnvironment(this, values);
    }

    public Node lookup(String variable) {
        Supplier<Node> value = _variableMap.get(variable);
        if (value != null) {
            return value.get();
        }
        return _parent.lookup(variable);
    }

    public Function<List<Node>, Node> getFunction(String name) {
        return _parent.getFunction(name);
    }
}
