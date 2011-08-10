package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.eval.annotations.LessFunction;
import com.bazaarvoice.jless.eval.annotations.NumberValue;
import com.bazaarvoice.jless.eval.annotations.StringValue;
import com.bazaarvoice.jless.exception.FunctionException;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;
import org.apache.commons.lang.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class FunctionImpl implements Function<List<Node>, Node> {

    private final Object _instance;
    private final Method _method;
    private final String _name;
    private final List<Function<Node, ?>> _argAdapters;
    private final Class _varargType;

    public static FunctionImpl wrap(Object instance, Method method) {
        LessFunction less = getLessAnnotation(method);
        if (less == null) {
            return null;
        }

        String name = !"".equals(less.name()) ? less.name() : method.getName();

        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        List<Function<Node, ?>> argAdapters = new ArrayList<Function<Node, ?>>(parameterAnnotations.length);
        for (int i = 0; i < parameterTypes.length; i++) {
            argAdapters.add(getArgAdapter(parameterTypes[i], parameterAnnotations[i]));
        }

        return new FunctionImpl(instance, method, name, argAdapters);
    }

    public FunctionImpl(Object instance, Method method, String name, List<Function<Node, ?>> argAdapters) {
        _instance = instance;
        _method = method;
        _name = name;
        _argAdapters = argAdapters;
        _varargType = _method.isVarArgs() ? _method.getParameterTypes()[argAdapters.size() - 1].getComponentType() : null;
    }

    private static LessFunction getLessAnnotation(Method method) {
        for (Annotation annotation : method.getAnnotations()) {
            if (annotation instanceof LessFunction) {
                return (LessFunction) annotation;
            }
        }
        return null;
    }

    private static Function<Node, ?> getArgAdapter(Class<?> type, Annotation[] annotations) {
        if (type.isArray()) {
            type = type.getComponentType();
        }
        for (Annotation annotation : annotations) {
            if (annotation instanceof NumberValue) {
                return Coercions.NUMBER_VALUE_ADAPTER;
            }
            if (annotation instanceof StringValue) {
                return Coercions.STRING_VALUE_ADAPTER;
            }
        }
        if (type == Double.class || type == Double.TYPE) {
            return Coercions.NUMBER_ADAPTER;
        }
        if (type == String.class) {
            return Coercions.STRING_ADAPTER;
        }
        if (!Node.class.isAssignableFrom(type)) {
            throw new IllegalArgumentException(type.toString());
        }
        return null;  // no coercion necessary
    }

    public String getName() {
        return _name;
    }

    @Override
    public Node apply(List<Node> arguments) {
        int firstVarArgIndex = _method.isVarArgs() ? _argAdapters.size() - 1 : -1;
        if (firstVarArgIndex >= 0) {
            if (arguments.size() < firstVarArgIndex) {
                throw new FunctionException("Incorrect number of arguments: " + _name + "(" + StringUtils.join(arguments, ", ") + "...)");
            }
        } else {
            if (arguments.size() != _argAdapters.size()) {
                throw new FunctionException("Incorrect number of arguments: " + _name + "(" + StringUtils.join(arguments, ", ") + ")");
            }
        }
        Object[] convertedArgs = new Object[_argAdapters.size()];
        for (int i = 0; i < _argAdapters.size(); i++) {
            Function<Node, ?> adapter = _argAdapters.get(i);
            if (i == firstVarArgIndex) {
                Object[] varargs = (Object[]) Array.newInstance(_varargType, arguments.size() - firstVarArgIndex);
                for (int j = 0; j < varargs.length; j++) {
                    varargs[j] = (adapter != null) ? adapter.apply(arguments.get(i + j)) : arguments.get(i + j);
                }
                convertedArgs[i] = varargs;
            } else {
                convertedArgs[i] = (adapter != null) ? adapter.apply(arguments.get(i)) : arguments.get(i);
            }
        }
        try {
            return (Node) _method.invoke(_instance, convertedArgs);
        } catch (Exception e) {
            throw new FunctionException("Exception evaluation function: " + _name + "(" + StringUtils.join(arguments, ", ") + ")", e);
        }
    }
}
