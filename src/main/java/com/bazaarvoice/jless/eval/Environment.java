package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Ruleset;
import com.google.common.base.Function;

import java.util.List;

public interface Environment {

    Environment extend(Ruleset ruleset);

    Node getVariable(String variable);

    Function<List<Node>, Node> getFunction(String name);

    List<Ruleset> getRulesets(List<Element> elements);
}
