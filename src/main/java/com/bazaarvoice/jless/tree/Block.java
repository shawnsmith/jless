package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class Block extends NodeWithPosition {

    private final boolean _root;
    private final List<Node> _statements;

    public Block(int position, boolean root, List<Node> statements) {
        super(position);
        _root = root;
        _statements = statements;
    }

    public boolean isRoot() {
        return _root;
    }

    public boolean isEmpty() {
        return _statements.isEmpty();
    }

    public List<Node> getStatements() {
        return _statements;
    }

    @Override
    public Block eval(Environment env) {
        if (!isEmpty()) {
            List<Node> results = new ArrayList<Node>(_statements.size());
            for (Node statement : _statements) {
                results.add(statement.eval(env));
            }
            return new Block(getPosition(), _root, results);
        } else {
            return this;
        }
    }

    @Override
    public Block flatten(List<Selector> contexts, List<Node> flattenedRulesets) {
        if (!isEmpty()) {
            List<Node> results = new ArrayList<Node>(_statements.size());
            for (Node statement : _statements) {
                statement = statement.flatten(contexts, flattenedRulesets);
                if (statement != null) {
                    results.add(statement);
                }
            }
            return new Block(getPosition(), _root, results);
        } else {
            return this;
        }
    }

    @Override
    public void printCSS(CssWriter out) {
        if (!_root) {
            out.print('{');
            out.newline();
            out.beginScope();
        }
        for (Node statement : _statements) {
            statement.printCSS(out);
        }
        if (!_root) {
            out.endScope();
            out.indent(this);
            out.print('}');
            out.newline();
        }
    }

    @Override
    public String toString() {
        return (_root ? "" : "{") + StringUtils.join(_statements, "\n") + (_root ? "" : "}");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Block", _root, _statements);
    }
}
