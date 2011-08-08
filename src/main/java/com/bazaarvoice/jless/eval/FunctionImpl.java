package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.eval.annotations.*;
import com.bazaarvoice.jless.exception.FunctionException;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FunctionImpl implements Function<List<Node>, Node> {

    private final Method _method;
    private final int _arity;
    private final List<Function<Node, ?>> _argAdapters;

    public FunctionImpl(Method method) {
        _method = method;

        Class<?>[] argTypes = _method.getParameterTypes();
        Annotation[][] argAnnotations = _method.getParameterAnnotations();
        _arity = argTypes.length;
        _argAdapters = new ArrayList<Function<Node, ?>>(argTypes.length);
        for (int i = 0; i < argTypes.length; i++) {
            _argAdapters.add(getAdapter(argTypes[i], argAnnotations[i]));
        }
    }

    private Function<Node, ?> getAdapter(Class<?> type, Annotation[] annotations) {
        for (Annotation annotation : annotations) {
            if (annotation instanceof com.bazaarvoice.jless.eval.annotations.Number) {
                return Coercions.NUMBER_ADAPTER;
            }
            if (annotation instanceof Value) {
                return Coercions.VALUE_ADAPTER;
            }
        }
        return null;
    }

    @Override
    public Node apply(List<Node> args) {
        if (args.size() != _arity) {
            throw new FunctionException("Incorrect number of arguments: " + _method.getName() + "(" + StringUtils.join(args, ", ") + ")");
        }
        Object[] convertedArgs = new Object[_arity];
        for (int i = 0; i < _arity; i++) {
            Function<Node, ?> adapter = _argAdapters.get(i);
            convertedArgs[i] = (adapter != null) ? adapter.apply(args.get(i)) : args.get(i);
        }
        try {
            return (Node) _method.invoke(null, convertedArgs);
        } catch (Exception e) {
            throw new FunctionException("Exception evaluation function: " + _method.getName() + "(" + StringUtils.join(args, ", ") + ")", e);
        }
    }
}
