package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Ruleset;
import com.google.common.base.Function;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GlobalEnvironment implements Environment {

    private final Map<String, Function<List<Node>, Node>> _functions;

    public GlobalEnvironment(Map<String, Function<List<Node>, Node>> functions) {
        _functions = functions;
    }

    @Override
    public Environment extend(Ruleset ruleset) {
        return new LocalEnvironment(this, ruleset);
    }

    @Override
    public Node getVariable(String variable) {
        return null;
    }

    @Override
    public Function<List<Node>, Node> getFunction(String name) {
        return _functions.get(name);
    }

    @Override
    public List<Ruleset> getRulesets(List<Element> elements) {
        return Collections.emptyList();
    }
}
