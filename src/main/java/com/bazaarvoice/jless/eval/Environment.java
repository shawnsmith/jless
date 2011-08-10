package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Block;
import com.bazaarvoice.jless.tree.Closure;
import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.util.List;

public interface Environment {

    Environment extend(Block frame);

    Function<List<Node>, Node> getFunction(String name);

    Node getVariable(String variable);

    List<Closure> getClosures(List<Element> elements);
}
