package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.util.List;
import java.util.Map;

public interface Environment {

    Environment extend(Map<String, Node> values);

    Node lookup(String variable);

    Function<List<Node>, Node> getFunction(String name);
}
