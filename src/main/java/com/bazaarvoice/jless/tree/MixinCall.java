package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.UndefinedMixinException;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class MixinCall extends Node {

    private final Selector _selector;
    private final List<Expression> _arguments;

    public MixinCall(List<Element> elements, List<Expression> arguments) {
        _selector = new Selector(elements);
        _arguments = arguments;
    }

    @Override
    public Node eval(Environment env) {
        List<Ruleset> rulesets = env.getRulesets(_selector.getElements());
        if (rulesets.isEmpty()) {
            throw new UndefinedMixinException(_selector.toString().trim() + " is undefined");
        }

        List<Node> arguments = new ArrayList<Node>(_arguments.size());
        for (Expression argument : _arguments) {
            arguments.add(argument.eval(env));
        }

        List<Node> results = new ArrayList<Node>();
        boolean match = false;
        for (Ruleset ruleset : rulesets) {
            if (ruleset.match(arguments, env)) {
                results.addAll(ruleset.eval(env, arguments).rules);
                match = true;
            }
        }
        if (!match) {
            throw new UndefinedMixinException("No matching definition was found for `" + _selector.toString().trim() + "(" + StringUtils.join(arguments, ", ") + ")");
        }

        return !results.isEmpty() ? new Block(0, false, results) : null;
    }

    @Override
    public void printCSS(CssWriter out) {
        out.print(_selector);
        out.print('(');
        out.print(_arguments, ",", ", ");
        out.print(')');
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("MixinCall", _selector, _arguments);
    }
}
