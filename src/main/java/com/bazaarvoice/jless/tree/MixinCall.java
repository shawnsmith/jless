package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class MixinCall extends Node {

    private final Selector _selector;
    private final List<Expression> _arguments;

    public MixinCall(List<Element> elements, List<Expression> arguments) {
        _selector = new Selector(elements);
        _arguments = arguments;
    }

    @Override
    public Node eval(Environment env) {
        // TODO
        return super.eval(env);
    }

    @Override
    public String toCSS(Environment env) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return _selector + "(" + StringUtils.join(_arguments, ", ") + ")";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinCall", _selector, _arguments);
    }
}
