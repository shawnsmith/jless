package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class MixinDefinitionParameter extends Node {

    private final Variable _name;
    private final Node _value;

    public MixinDefinitionParameter(Variable name, Node value) {
        _name = name;
        _value = value;
    }

    public Variable getName() {
        return _name;
    }

    public Node getValue() {
        return _value;
    }

    @Override
    public void printCSS(CssWriter out) {
        // do nothing
    }

    @Override
    public String toString() {
        return (_name != null ? _name : "") +
                (_name != null && _value != null ? ": " : "") +
                (_value != null ? _value : "");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinDefinitionParameter", _name, _value);
    }
}
