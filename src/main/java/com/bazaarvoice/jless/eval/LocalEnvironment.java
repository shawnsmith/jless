package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Ruleset;
import com.google.common.base.Function;
import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LocalEnvironment implements Environment {

    private final Environment _parent;
    private final Ruleset _ruleset;
    private final Map<String, Supplier<Node>> _variableMap;

    public LocalEnvironment(Environment parent, Ruleset ruleset) {
        _parent = parent;
        _ruleset = ruleset;

        // variables are evaluated lazily in their own context.  basically this implements lexical scoping.
        _variableMap = new HashMap<String, Supplier<Node>>();
        final Environment self = this;
        for (Map.Entry<String, Node> entry : ruleset.getVariables().entrySet()) {
            String key = entry.getKey();
            final Node value = entry.getValue();
            _variableMap.put(key, Suppliers.memoize(new Supplier<Node>() {
                public Node get() {
                    return value.eval(self);
                }
            }));
        }
    }

    @Override
    public Environment extend(Ruleset ruleset) {
        return new LocalEnvironment(this, ruleset);
    }

    @Override
    public Node getVariable(String variable) {
        Supplier<Node> value = _variableMap.get(variable);
        if (value != null) {
            return value.get();
        }
        return _parent.getVariable(variable);
    }

    @Override
    public Function<List<Node>, Node> getFunction(String name) {
        return _parent.getFunction(name);
    }

    @Override
    public List<Ruleset> getRulesets(List<Element> elements) {
        List<Ruleset> rulesets = _ruleset.findRulesets(elements);
        if (!rulesets.isEmpty()) {
            return rulesets;
        }
        return _parent.getRulesets(elements);
    }
}
