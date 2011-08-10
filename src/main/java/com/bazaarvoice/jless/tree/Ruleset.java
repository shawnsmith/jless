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
    public void flatten(List<Selector> contexts, List<Node> parentBlock, List<Node> globalBlock) {
        List<Selector> localContexts = joinSelectors(contexts, _selectors);
        List<Node> currentBlock = new ArrayList<Node>();
        List<Node> tempGlobalBlock = new ArrayList<Node>();
        _rules.flatten(localContexts, currentBlock, tempGlobalBlock);
        if (!currentBlock.isEmpty()) {
            globalBlock.add(new Ruleset(getPosition(), localContexts, new Block(currentBlock)));
        }
        globalBlock.addAll(tempGlobalBlock);
    }

    private List<Selector> joinSelectors(List<Selector> contexts, List<Selector> selectors) {
        List<Selector> results = new ArrayList<Selector>();
        for (Selector selector : selectors) {
            List<Element> beforeElements = new ArrayList<Element>();
            List<Element> afterElements = new ArrayList<Element>();

            boolean hasParentSelector = false;
            for (Element element : selector.getElements()) {
                if (element.getCombinator().getStringValue().startsWith("&")) {
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

    @Override
    public void printCss(CssWriter out) {
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
            CssWriter sub = out.subWriter();
            sub.print(_selectors.get(i));
            out.print(sub.toString().trim());
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
}
