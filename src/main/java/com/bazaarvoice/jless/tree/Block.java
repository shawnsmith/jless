package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Block extends Node {

    private final List<Node> _statements;
    private Map<String, Node> _variables;
    private Map<String, Set<Ruleset>> _rulesets;
    private Map<List<Element>, List<Closure>> _lookups;

    public Block(List<Node> statements) {
        _statements = statements;
    }

    public boolean isEmpty() {
        return _statements.isEmpty();
    }

    public List<Node> getStatements() {
        return _statements;
    }

    @Override
    public Block eval(Environment env) {
        if (isEmpty()) {
            return this;
        }

        Environment localEnv = env.extend(this);

        List<Node> results = new ArrayList<Node>(_statements.size());
        for (Node statement : _statements) {
            Node evaluated = statement.eval(localEnv);
            if (evaluated != null) {
                if (evaluated instanceof Block) {
                    results.addAll(((Block) evaluated).getStatements());
                } else {
                    results.add(evaluated);
                }
            }
        }
        return new Block(results);
    }

    public Block flatten() {
        List<Node> flattenedRulesets = new ArrayList<Node>();
        flatten(Collections.singletonList(new Selector(true)), flattenedRulesets);
        return new Block(flattenedRulesets);
    }

    @Override
    public Block flatten(List<Selector> contexts, List<Node> flattenedRulesets) {
        if (isEmpty()) {
            return this;
        }
        List<Node> results = new ArrayList<Node>(_statements.size());
        for (Node statement : _statements) {
            statement = statement.flatten(contexts, flattenedRulesets);
            if (statement != null) {
                results.add(statement);
            }
        }
        return new Block(results);
    }

    @Override
    public void printCSS(CssWriter out) {
        out.print(_statements, "", "");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Block", _statements);
    }

    public Map<String, Node> getVariables() {
        if (_variables == null) {
            _variables = new HashMap<String, Node>();
            for (Node statement : _statements) {
                if (statement instanceof Rule) {
                    Rule rule = (Rule) statement;
                    if (rule.getName() instanceof Variable) {
                        _variables.put(((Variable) rule.getName()).getName(), rule.getValue());
                    }
                }
            }
        }
        return _variables;
    }

    public List<Closure> getClosures(List<Element> elements) {
        if (_lookups == null) {
            _lookups = new HashMap<List<Element>, List<Closure>>();
        }
        List<Closure> closures = _lookups.get(elements);
        if (closures == null) {
            closures = new ArrayList<Closure>();
            Set<Ruleset> rulesets = getRulesets().get(elements.get(0).getStringValue());
            if (rulesets != null) {
                for (Ruleset ruleset : rulesets) {
                    if (elements.size() > 1) {
                        closures.addAll(ruleset.getRules().getClosures(elements.subList(1, elements.size())));
                    } else {
                        closures.add(ruleset);
                    }
                }
            }
            _lookups.put(elements, closures);
        }
        return closures;
    }

    private Map<String, Set<Ruleset>> getRulesets() {
        if (_rulesets == null) {
            _rulesets = new HashMap<String, Set<Ruleset>>();
            for (Node statement : _statements) {
                if (statement instanceof Ruleset) {
                    Ruleset ruleset = (Ruleset) statement;
                    for (Selector selector : ruleset.getSelectors()) {
                        String name = selector.getElements().get(0).getStringValue();

                        Set<Ruleset> rulesets = _rulesets.get(name);
                        if (rulesets == null) {
                            rulesets = new LinkedHashSet<Ruleset>(); // maintain order so initial rules take precedence over later rules
                            _rulesets.put(name, rulesets);
                        }
                        rulesets.add(ruleset);
                    }
                }
            }
        }
        return _rulesets;
    }
}
