package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class MixinDefinition extends Node {

    private final String _name;
    private final List<MixinDefinitionParameter> _parameters;
    private final int _numRequired;
    private final Block _rules;
    private final Ruleset _ruleset;

    public MixinDefinition(String name, List<MixinDefinitionParameter> parameters, Block rules) {
        _name = name;
        _parameters = parameters;
        _rules = rules;
        _ruleset = new Ruleset(0, new Selector(new Element("", name)), _rules);

        int numRequired = 0;
        for (MixinDefinitionParameter parameter : parameters) {
            if (parameter.getName() == null || parameter.getValue() == null) {
                numRequired++;
            }
        }
        _numRequired = numRequired;
    }

    @Override
    public Node eval(Environment env) {
        throw new UnsupportedOperationException(); // TODO
    }

    @Override
    public void printCSS(CssWriter out) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String toString() {
        return _name + " " + "(" + StringUtils.join(_parameters, ", ") + ") " + _rules;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinDefinition", _name, _parameters, _rules);
    }
}
