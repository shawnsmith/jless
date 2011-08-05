package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.FunctionException;
import com.bazaarvoice.jless.parser.DebugPrinter;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.List;

//
// A function call node.
//
public class Call extends Node {

    private final String _name;
    private final List<Expression> _args;

    public Call(String name, List<Expression> args) {
        _name = name;
        _args = args;
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
        List<Node> args = new ArrayList<Node>(_args.size());
        for (Node arg : args) {
            args.add(arg.eval(env));
        }

        Function<List<Node>,Node> function = env.getFunction(_name);
        if (function != null) {
            try {
                return function.apply(args);
            } catch (Exception e) {
                throw new FunctionException("Error evaluating function " + _name + ": " + e);
            }
        } else {
            StringBuilder buf = new StringBuilder();
            buf.append(_name);
            buf.append('(');
            for (int i = 0; i < args.size(); i++) {
                if (i > 0) {
                    buf.append(',');
                    if (!env.isCompressionEnabled()) {
                        buf.append(' ');
                    }
                    buf.append(args.get(i).toCSS(env));
                }
            }
            buf.append(')');
            return new Anonymous(buf.toString());
        }
    }

    @Override
    public String toCSS(Environment env) {
        return eval(env).toCSS(env);
    }

    @Override
    public String toString() {
        return _name + "(" + StringUtils.join(_args, ", ") + ")";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Call", _name, _args);
    }
}
