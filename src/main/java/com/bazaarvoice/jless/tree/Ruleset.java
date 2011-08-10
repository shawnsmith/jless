package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.AppendEnvironment;
import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Ruleset extends NodeWithPosition implements Closure {
    private final List<Selector> _selectors;
    private final Block _rules;
    private Environment _lexicalEnvironment;

    public Ruleset(int position, Selector selector, Block rules) {
        this(position, Collections.singletonList(selector), rules);
    }

    public Ruleset(int position, List<Selector> selectors, Block rules) {
        super(position);
        _selectors = selectors;
        _rules = rules;
    }

    public List<Selector> getSelectors() {
        return _selectors;
    }

    public Block getRules() {
        return _rules;
    }

    public Environment getLexicalEnvironment() {
        return _lexicalEnvironment;
    }

    protected void setLexicalEnvironment(Environment env) {
        if (_lexicalEnvironment != null) {
            throw new IllegalStateException();  // setter may not be called multiple times
        }
        _lexicalEnvironment = env;
    }

    @Override
    public Ruleset eval(Environment env) {
        setLexicalEnvironment(env);
        return new Ruleset(getPosition(), _selectors, _rules.eval(env));
    }

    @Override
    public boolean match(List<Node> arguments, Environment dynamicEnvironment) {
        return arguments.isEmpty();
    }

    @Override
    public Block apply(List<Node> arguments, Environment dynamicEnvironment) {
        return _rules.eval(new AppendEnvironment(_lexicalEnvironment, dynamicEnvironment));
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
            out.print(_selectors.get(i).toString().trim());
        }
        if (!out.isCompressionEnabled()) {
            out.print(' ');
        }
        printRules(out);
    }

    protected void printRules(CssWriter out) {
        out.print('{');
        out.newline();
        out.beginScope();
        out.print(_rules);
        out.endScope();
        out.indent(this);
        out.print('}');
        out.newline();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Ruleset", _selectors, _rules);
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
