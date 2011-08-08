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
    private Map<String, Node> _variableMap;

    public Ruleset(int position, Block rules) {
        this(position, new Selector(true), rules);
        if (!rules.isRoot()) {
            throw new IllegalStateException();
        }
    }

    public Ruleset(int position, Selector selector, Block rules) {
        this(position, Collections.singletonList(selector), rules);
    }

    public Ruleset(int position, List<Selector> selectors, Block rules) {
        super(position);
        _selectors = selectors;
        _rules = rules;
    }

    public boolean isRoot() {
        return _rules.isRoot();
    }

    @Override
    public Node eval(Environment env) {
        Environment localEnv = env.extend(getVariableMap());
        Block rules = _rules.eval(localEnv);
        if (isRoot()) {
            List<Node> flattenedRulesets = new ArrayList<Node>();
            rules.flatten(_selectors, flattenedRulesets);
            return new Ruleset(getPosition(), new Block(getPosition(), true, flattenedRulesets));
        } else {
            return new Ruleset(getPosition(), _selectors, rules);
        }
    }

    @Override
    public Node flatten(List<Selector> contexts, List<Node> flattenedRulesets) {
        List<Selector> localContexts = joinSelectors(contexts, _selectors);
        List<Node> childFlattenedRulesets = new ArrayList<Node>();
        Block flattenedRules = _rules.flatten(localContexts, childFlattenedRulesets);
        if (!flattenedRules.isEmpty()) {
            flattenedRulesets.add(new Ruleset(getPosition(), localContexts, flattenedRules));
        }
        flattenedRulesets.addAll(childFlattenedRulesets);
        return null;
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        if (!isRoot()) {
            for (int i = 0; i < _selectors.size(); i++) {
                if (i > 0) {
                    out.print(',');
                    if (!out.isCompressionEnabled()) {
                        if (_selectors.size() > 3) {
                            out.newline();
                            out.indent(this);
                        } else {
                            out.print(' ');
                        }
                    }
                }
                CssWriter selectorOut = out.nestedWriter();
                _selectors.get(i).printCSS(selectorOut);
                out.print(selectorOut.toString().trim());
            }
            out.print(' ');
        }
        _rules.printCSS(out);
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        if (!isRoot()) {
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

    private Map<String, Node> getVariableMap() {
        if (_variableMap == null) {
            _variableMap = new HashMap<String, Node>();
            for (Node statement : _rules.getStatements()) {
                if (statement instanceof Rule) {
                    Rule rule = (Rule) statement;
                    if (rule.getName() instanceof Variable) {
                        _variableMap.put(((Variable) rule.getName()).getName(), rule.getValue());
                    }
                }
            }
        }
        return _variableMap;
    }

    private List<Selector> joinSelectors(List<Selector> contexts, List<Selector> selectors) {
        List<Selector> results = new ArrayList<Selector>();
        for (Selector selector : selectors) {
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

            for (Selector context : contexts) {
                List<Element> path = new ArrayList<Element>();
                if (!beforeElements.isEmpty()) {
                    path.addAll(new Selector(beforeElements).getElements());
                }
                path.addAll(context.getElements());
                if (!afterElements.isEmpty()) {
                    path.addAll(new Selector(afterElements).getElements());
                }
                results.add(new Selector(path));
            }
        }
        return results;
    }
}
