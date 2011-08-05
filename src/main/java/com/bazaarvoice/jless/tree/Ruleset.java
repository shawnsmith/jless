package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.parser.DebugPrinter;

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
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < _selectors.size(); i++) {
            if (i > 0) {
                buf.append(", ");
            }
            buf.append(_selectors.get(0).toString().trim());
        }
        buf.append(' ');
        buf.append(_rules);
        return buf.toString();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Ruleset", _selectors, _rules);
    }
}
