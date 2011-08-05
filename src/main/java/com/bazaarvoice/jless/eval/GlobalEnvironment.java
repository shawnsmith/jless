package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

public class GlobalEnvironment implements Environment {

    private boolean _compressionEnabled;
    private final Map<String, Function<List<Node>, Node>> _functions;

    public GlobalEnvironment(boolean compressionEnabled, Map<String, Function<List<Node>, Node>> functions) {
        _compressionEnabled = compressionEnabled;
        _functions = functions;
    }

    public Environment extend(Map<String, Node> values) {
        return new LocalEnvironment(this, values);
    }

    public Node lookup(String variable) {
        return null;
    }

    public Function<List<Node>, Node> getFunction(String name) {
        return _functions.get(name);
    }

    public boolean isCompressionEnabled() {
        return _compressionEnabled;
    }
}
