package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.FunctionException;
import com.bazaarvoice.jless.parser.DebugPrinter;
import com.google.common.base.Function;

import java.util.ArrayList;
import java.util.List;

//
// A function call node.
//
public class Call extends Node {

    private final String _name;
    private final List<Expression> _arguments;

    public Call(String name, List<Expression> arguments) {
        _name = name;
        _arguments = arguments;
    }

    //
    // When evaluating a function call,
    // we either find the function in `tree.functions` [1],
    // in which case we call it, passing the  evaluated arguments,
    // or we simply print it out as it appeared originally [2].
    //
    // The *functions.js* file contains the built-in functions.
    //
    // The reason why we evaluate the arguments, is in the case where
    // we try to pass a variable to a function, like: `saturate(@color)`.
    // The function should receive the value, not the variable.
    //
    @Override
    public Node eval(Environment env) {
        List<Node> arguments = new ArrayList<Node>(_arguments.size());
        for (Node arg : _arguments) {
            arguments.add(arg.eval(env));
        }

        Function<List<Node>,Node> function = env.getFunction(_name);
        if (function != null) {
            try {
                return function.apply(arguments);
            } catch (Exception e) {
                throw new FunctionException("Error evaluating function " + _name + ": " + e);
            }
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(_name);
            buf.append('(');
            for (int i = 0; i < arguments.size(); i++) {
                if (i > 0) {
                    buf.append(',');
                }
                buf.append(arguments.get(i));
            }
            buf.append(')');
            return new Anonymous(buf.toString());
        }
    }

    @Override
    public void printCss(CssWriter out) {
        out.print(_name);
        out.print("(");
        out.print(_arguments, ",", ", ");
        out.print(')');
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Call", _name, _arguments);
    }
}
