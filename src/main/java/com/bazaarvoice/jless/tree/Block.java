package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.List;

public class Block extends Node {

    private final boolean _root;
    private final List<Node> _statements;

    public Block(boolean root, List<Node> statements) {
        _root = root;
        _statements = statements;
    }

    @Override
    public Node eval(Environment env) {
        if (_statements.size() > 0) {
            List<Node> results = new ArrayList<Node>(_statements.size());
            for (Node statement : _statements) {
                results.add(statement.eval(env));
            }
            return new Block(_root, results);
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
        StringBuilder buf = new StringBuilder();
        if (!_root) {
            buf.append("{\n");
        }
        for (Node statement : _statements) {
            if (!_root) {
                buf.append("  ");
            }
            buf.append(statement).append('\n');
        }
        if (!_root) {
            buf.append("}\n");
        }
        return buf.toString();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Block", _root, _statements);
    }
}
