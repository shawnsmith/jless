package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Block extends Node {

    private final List<Node> _statements;

    public Block(Node statement) {
        this(Collections.singletonList(statement));
    }

    public Block(List<Node> statements) {
        _statements = statements;
    }

    @Override
    public Node eval(Environment env) {
        if (_statements.size() > 1) {
            List<Node> results = new ArrayList<Node>(_statements.size());
            for (Node statement : _statements) {
                results.add(statement.eval(env));
            }
            return new Block(results);
        } else if (_statements.size() == 1) {
            return _statements.get(0).eval(env);
        } else {
            return this;
        }
    }

    @Override
    public String toCSS(Environment env) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < _statements.size(); i++) {
            if (i > 0) {
                buf.append('\n');
            }
            buf.append(_statements.get(i).toCSS(env));
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        return StringUtils.join(_statements, '\n');
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Block", _statements);
    }
}
