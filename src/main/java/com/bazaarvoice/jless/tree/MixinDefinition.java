package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.UndefinedMixinException;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.List;

public class MixinDefinition extends Ruleset {

    private final String _name;
    private final List<MixinDefinitionParameter> _parameters;
    private final int _numRequired;

    public MixinDefinition(int position, String name, List<MixinDefinitionParameter> parameters, Block rules) {
        super(position, new Selector(new Element("", name)), rules);
        _name = name;
        _parameters = parameters;

        int numRequired = 0;
        for (MixinDefinitionParameter parameter : parameters) {
            if (parameter.getName() == null || parameter.getValue() == null) {
                numRequired++;
            }
        }
        _numRequired = numRequired;
    }

    @Override
    public Node eval(Environment env) {
        return null;
    }

    @Override
    public List<Node> eval(Environment env, List<Node> arguments) {
        for (int i = 0; i < _parameters.size(); i++) {
            Variable name = _parameters.get(i).getName();
            if (name != null) {
                Node val = (i < arguments.size()) ? arguments.get(i) : _parameters.get(i).getValue();
                if (val == null) {
                    throw new UndefinedMixinException("Wrong number of arguments for " + _name + " (" + arguments.size() + " for " + _parameters.size());
                }
                frame.rules.unshift(new(tree.Rule)(this.params[i].name, val.eval(env)));
            }
        }

        List<Node> argumentsValue = new ArrayList<Node>();
        for (int i = 0; i < Math.max(_parameters.size(), arguments.size()); i++) {
            argumentsValue.add((i < arguments.size()) ? arguments.get(i) : _parameters.get(i).getValue());
        }
        frame.rules.unshift(new(tree.Rule)('@arguments', new(tree.Expression)(_arguments).eval(env)));

        getRules().eval(env.extend(frame).extend(this));  // lexical freaking scoping!

        return new(tree.Ruleset)(null, this.rules.slice(0)).eval({
            frames: [this, frame].concat(this.frames, env.frames)
        });
    }

    public boolean match(List<Node> arguments, Environment env) {
        int argsLength = arguments.size();
        if (argsLength < _numRequired || (_numRequired > 0 && argsLength > _parameters.size())) {
            return false;
        }
        int len = Math.min(argsLength, _parameters.size());
        for (int i = 0; i < len; i++) {
            if (_parameters.get(i).getName() == null) {
                if (!arguments.get(i).toString().equals(_parameters.get(i).getValue().eval(env).toString())) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void printCSS(CssWriter out) {
        out.print(_name);
        out.print(" (");
        out.print(_parameters, ",", ", ");
        out.print(") ");
        out.print(getRules());
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinDefinition", _name, _parameters, getRules());
    }
}
