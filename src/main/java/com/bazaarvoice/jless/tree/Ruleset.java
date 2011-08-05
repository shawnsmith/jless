package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.List;

public class Ruleset extends Node {
    private final List<Selector> _selectors;
    private final Block _rules;

    public Ruleset(List<Selector> selectors, Block rules) {
        _selectors = selectors;
        _rules = rules;
    }

    @Override
    public String toString() {
        return StringUtils.join(_selectors, ", ") + " { " + _rules + " }\n";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Ruleset", _selectors, _rules);
    }
}
