package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ruleset extends NodeWithPosition {
    private final List<Selector> _selectors;
    private final Block _rules;
    private final Map<String, Node> _variableMap;

    public Ruleset(int position, Block rules) {
        this(position, Collections.<Selector>emptyList(), rules);
    }

    public Ruleset(int position, Selector selector, Block rules) {
        this(position, Collections.singletonList(selector), rules);
    }

    public Ruleset(int position, List<Selector> selectors, Block rules) {
        super(position);
        _selectors = selectors;
        _rules = rules;

        _variableMap = new HashMap<String, Node>();
        for (Node statement : rules.getStatements()) {
            if (statement instanceof Rule) {
                Rule rule = (Rule) statement;
                if (rule.getName() instanceof Variable) {
                    _variableMap.put(((Variable) rule.getName()).getName(), rule.getValue());
                }
            }
        }
    }

    @Override
    public Node eval(Environment env) {
        Environment localEnv = env.extend(_variableMap);

        return new Ruleset(getPosition(), _selectors, _rules.eval(localEnv));
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        if (!_selectors.isEmpty()) {
            for (int i = 0; i < _selectors.size(); i++) {
                if (i > 0) {
                    out.print(", ");
                }
                out.print(_selectors.get(i).toString().trim());
            }
            out.print(' ');
        }
        _rules.printCSS(out);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (!_selectors.isEmpty()) {
            for (int i = 0; i < _selectors.size(); i++) {
                if (i > 0) {
                    buf.append(", ");
                }
                buf.append(_selectors.get(0).toString().trim());
            }
            buf.append(' ');
        }
        buf.append(_rules);
        return buf.toString();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Ruleset", _selectors, _rules);
    }

    private void joinSelectors(List<List<Selector>> paths, List<List<Selector>> context, List<Selector> selectors) {
        for (Selector selector : selectors) {
            joinSelector(paths, context, selector);
        }
    }

    private void joinSelector(List<List<Selector>> paths, List<List<Selector>> context, Selector selector) {
        List<Element> beforeElements = new ArrayList<Element>();
        List<Element> afterElements = new ArrayList<Element>();

        boolean hasParentSelector = false;
        for (Element element : selector.getElements()) {
            if (element.getCombinator().getValue().startsWith("&")) {
                hasParentSelector = true;
            }
            (hasParentSelector ? afterElements : beforeElements).add(element);
        }

        if (!hasParentSelector) {
            afterElements = beforeElements;
            beforeElements = Collections.emptyList();
        }

        Selector before = !beforeElements.isEmpty() ? new Selector(beforeElements) : null;
        Selector after = !afterElements.isEmpty() ? new Selector(afterElements) : null;

        for (List<Selector> cxt : context) {
            List<Selector> path = new ArrayList<Selector>();
            if (before != null) {
                path.add(before);
            }
            path.addAll(cxt);
            if (after != null) {
                path.add(after);
            }
            paths.add(path);
        }
    }
}
