package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Block;
import com.bazaarvoice.jless.tree.Closure;
import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
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
    public Environment extend(Block frame) {
        return new LocalEnvironment(this, frame);
    }

    @Override
    public Function<List<Node>, Node> getFunction(String name) {
        return _functions.get(name);
    }

    @Override
    public Node getVariable(String variable) {
        return null;
    }

    @Override
    public List<Closure> getClosures(List<Element> elements) {
        return Collections.emptyList();
    }
}
