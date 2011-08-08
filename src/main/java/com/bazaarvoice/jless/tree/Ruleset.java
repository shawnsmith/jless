package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.List;

public class Ruleset extends NodeWithPosition {
    private final List<Selector> _selectors;
    private final Block _rules;

    public Ruleset(int position, List<Selector> selectors, Block rules) {
        super(position);
        _selectors = selectors;
        _rules = rules;
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        out.indent(this);
        for (int i = 0; i < _selectors.size(); i++) {
            if (i > 0) {
                out.print(", ");
            }
            out.print(_selectors.get(0).toString().trim());
        }
        out.print(' ');
        _rules.printCSS(env, out);
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
