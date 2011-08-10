package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Block;
import com.bazaarvoice.jless.tree.Closure;
import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalEnvironment implements Environment {

    private final Environment _parent;
    private final Block _frame;
    private final Map<String, Supplier<Node>> _variables;

    public LocalEnvironment(Environment parent, Block frame) {
        _parent = parent;
        _frame = frame;

        // variables are evaluated lazily in their own context.  basically this implements lexical scoping.
        _variables = new HashMap<String, Supplier<Node>>();
        final Environment self = this;
        for (Map.Entry<String, Node> entry : frame.getVariables().entrySet()) {
            String key = entry.getKey();
            final Node value = entry.getValue();
            _variables.put(key, Suppliers.memoize(new Supplier<Node>() {
                public Node get() {
                    return value.eval(self);
                }
            }));
        }
    }

    @Override
    public Environment extend(Block frame) {
        return new LocalEnvironment(this, frame);
    }

    @Override
    public Function<List<Node>, Node> getFunction(String name) {
        return _parent.getFunction(name);
    }

    @Override
    public Node getVariable(String variable) {
        Supplier<Node> value = _variables.get(variable);
        return (value != null) ? value.get() : _parent.getVariable(variable);
    }

    @Override
    public List<Closure> getClosures(List<Element> elements) {
        List<Closure> closures = _frame.getClosures(elements);
        return !closures.isEmpty() ? closures : _parent.getClosures(elements);
    }
}
