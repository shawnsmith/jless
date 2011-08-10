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
    public void printCss(CssWriter out) {
        if (_name != null) {
            out.print(_name);
        }
        if (_name != null && _value != null) {
            out.print(": ");
        }
        if (_value != null) {
            out.print(_value);
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinDefinitionParameter", _name, _value);
    }
}
