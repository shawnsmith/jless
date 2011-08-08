package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

public class Directive extends NodeWithPosition {

    private final String _name;
    private final Block _rules;
    private final Ruleset _ruleset;
    private final Node _value;

    public Directive(int position, String name, Block rules) {
        super(position);
        _name = name;
        _rules = rules;
        _ruleset = new Ruleset(position, rules);
        _value = null;
    }

    public Directive(int position, String name, Node value) {
        super(position);
        _name = name;
        _rules = null;
        _ruleset = null;
        _value = value;
    }

    @Override
    public Node getValue() {
        return _value;
    }

    @Override
    public Node eval(Environment env) {
        if (_rules != null) {
            throw new UnsupportedOperationException(); // TODO
        } else {
            return this;
        }
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        out.print(_name);
        out.print(' ');
        if (_rules != null) {
            _rules.printCSS(out);
        } else {
            _value.printCSS(out);
            out.print(';');
            out.newline();
        }
    }

    @Override
    public String toString() {
        if (_ruleset != null) {
            return _name + " " + _ruleset;
        } else {
            return _name + " " + _value + ";";
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Directive", _name, _ruleset != null ? _ruleset : _value);
    }
/*
    tree.Directive.prototype = {
        toCSS: function (ctx, env) {
            if (this.ruleset) {
                this.ruleset.root = true;
                return this.name + (env.compress ? '{' : ' {\n  ') +
                       this.ruleset.toCSS(ctx, env).trim().replace(/\n/g, '\n  ') +
                                   (env.compress ? '}': '\n}\n');
            } else {
                return this.name + ' ' + this.value.toCSS() + ';\n';
            }
        },
        eval: function (env) {
            env.frames.unshift(this);
            this.ruleset = this.ruleset && this.ruleset.eval(env);
            env.frames.shift();
            return this;
        },
        variable: function (name) { return tree.Ruleset.prototype.variable.call(this.ruleset, name) },
        find: function () { return tree.Ruleset.prototype.find.apply(this.ruleset, arguments) },
        rulesets: function () { return tree.Ruleset.prototype.rulesets.apply(this.ruleset) }
    */
}