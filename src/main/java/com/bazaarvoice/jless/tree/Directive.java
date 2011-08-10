package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Directive extends NodeWithPosition {

    private final String _name;
    private final Node _value;

    public Directive(int position, String name, Node value) {
        super(position);
        _name = name;
        _value = value;
    }

    @Override
    public Node eval(Environment env) {
        return new Directive(getPosition(), _name, _value.eval(env));
    }

    @Override
    public Node flatten(List<Selector> contexts, List<Node> flattenedRulesets) {
        if (_value instanceof Block) {
            List<Node> directiveFlattenedRulesets = new ArrayList<Node>();
            List<Node> childFlattenedRulesets = new ArrayList<Node>();
            Block flattenedRules = ((Block) _value).flatten(contexts, childFlattenedRulesets);
            if (!flattenedRules.isEmpty()) {
                directiveFlattenedRulesets.addAll(flattenedRules.getStatements());
            }
            directiveFlattenedRulesets.addAll(childFlattenedRulesets);

            flattenedRulesets.add(new Directive(getPosition(), _name, new Block(directiveFlattenedRulesets)));
        } else {
            flattenedRulesets.add(this);
        }
        return null;
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        out.print(_name);
        if (_value instanceof Block) {
            if (!out.isCompressionEnabled()) {
                out.print(' ');
            }
            out.print('{');
            out.newline();
            out.beginScope();
            out.print(_value);
            out.endScope();
            out.indent(this);
            out.print('}');
        } else {
            out.print(' ');
            out.print(_value);
            out.print(';');
        }
        out.newline();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Directive", _name, _value);
    }
}