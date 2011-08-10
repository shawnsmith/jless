package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Block;
import com.bazaarvoice.jless.tree.Closure;
import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.util.List;

public class AppendEnvironment implements Environment {

    private final Environment _env1;
    private final Environment _env2;

    public AppendEnvironment(Environment env1, Environment env2) {
        _env1 = env1;
        _env2 = env2;
    }

    @Override
    public Environment extend(Block frame) {
        return new LocalEnvironment(this, frame);
    }

    @Override
    public Function<List<Node>, Node> getFunction(String name) {
        Function<List<Node>, Node> function = _env1.getFunction(name);
        return (function != null) ? function : _env2.getFunction(name);
    }

    @Override
    public Node getVariable(String variable) {
        Node value = _env1.getVariable(variable);
        return (value != null) ? value : _env2.getVariable(variable);
    }

    @Override
    public List<Closure> getClosures(List<Element> elements) {
        List<Closure> closures = _env1.getClosures(elements);
        return !closures.isEmpty() ? closures : _env2.getClosures(elements);
    }
}
