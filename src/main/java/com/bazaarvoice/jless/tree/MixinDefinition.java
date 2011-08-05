package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.Collections;
import java.util.List;

public class MixinDefinition extends Node {

    private final String _name;
    private final List<Selector> _selectors;
    private final List<MixinDefinitionParameter> _parameters;
    private final int _required;
    private final Block _rules;

    public MixinDefinition(String name, List<MixinDefinitionParameter> parameters, Block rules) {
        _name = name;
        _selectors = Collections.singletonList(new Selector(new Element("", name)));
        _parameters = parameters;
        _rules = rules;

        int required = 0;
        for (MixinDefinitionParameter parameter : parameters) {
            if (parameter.getName() == null || parameter.getValue() == null) {
                required++;
            }
        }
        _required = required;
    }

    @Override
    public String toCSS(Environment env) {
        return "";
    }

    @Override
    public String toString() {
        return _name + " " + "(" + StringUtils.join(_parameters, ", ") + ") {" + _rules + "}";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinDefinition", _name, _parameters, _rules);
    }
}
