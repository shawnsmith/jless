package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.exception.FunctionException;
import com.bazaarvoice.jless.tree.Dimension;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

public class Coercions {

    public static double number(Node node) {
        if (node instanceof Dimension) {
            Dimension dim = (Dimension) node;
            return "%".equals(dim.getUnit()) ? dim.getValue() / 100 : dim.getValue();
        }
        throw new FunctionException("Color functions take numbers as parameters: " + node);
    }

    public static final Function<Node, Double> NUMBER_ADAPTER = new Function<Node, Double>() {
        @Override
        public Double apply(Node node) {
            return number(node);
        }
    };

    public static double numberValue(Node node) {
        if (node instanceof Dimension) {
            return ((Dimension) node).getValue();
        }
        throw new FunctionException("Color functions take numbers as parameters: " + node);
    }

    public static final Function<Node, Double> NUMBER_VALUE_ADAPTER = new Function<Node, Double>() {
        @Override
        public Double apply(Node node) {
            return numberValue(node);
        }
    };

    public static final Function<Node, String> STRING_ADAPTER = new Function<Node, String>() {
        @Override
        public String apply(Node node) {
            return node.toString();
        }
    };

    public static final Function<Node, String> STRING_VALUE_ADAPTER = new Function<Node, String>() {
        @Override
        public String apply(Node node) {
            return node.getStringValue();
        }
    };
}
