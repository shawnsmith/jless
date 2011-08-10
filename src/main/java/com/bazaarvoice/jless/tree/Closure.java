package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;

import java.util.List;

public interface Closure {

    boolean match(List<Node> arguments, Environment dynamicEnvironment);

    Block apply(List<Node> arguments, Environment dynamicEnvironment);
}
