package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.MixinDefinition;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Selector;

import java.util.List;

public interface Frame {

    Node getVariable(String name);

    List<MixinDefinition> getMixins(Selector selector);
}
