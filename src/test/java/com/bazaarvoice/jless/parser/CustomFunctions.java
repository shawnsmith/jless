package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.eval.annotations.LessFunction;
import com.bazaarvoice.jless.eval.annotations.NumberValue;
import com.bazaarvoice.jless.tree.Color;
import com.bazaarvoice.jless.tree.Dimension;
import com.bazaarvoice.jless.tree.Node;

@SuppressWarnings( {"UnusedDeclaration"})
public class CustomFunctions {

    @LessFunction
    public Dimension add(@NumberValue double a, @NumberValue double b) {
        return new Dimension(a + b);
    }

    @LessFunction
    public Dimension increment(@NumberValue double a) {
        return new Dimension(a + 1);
    }

    @LessFunction
    public Node color(Node node) {
        if (node.getStringValue().equals("evil red")) {
            return new Color("600");
        }
        return node;
    }

}
