package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.Collections;

public class Directive extends Node {

    private final String _name;
    private final Ruleset _ruleset;
    private final Node _value;

    public Directive(String name, Block rules) {
        _name = name;
        _ruleset = new Ruleset(Collections.<Selector>emptyList(), rules);
        _value = null;
    }

    public Directive(String name, Node value) {
        _name = name;
        _ruleset = null;
        _value = value;
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