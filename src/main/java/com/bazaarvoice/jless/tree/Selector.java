package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Selector extends Node {

    private final List<Element> _elements;

    public Selector(Element element) {
        this(Collections.singletonList(element));
    }

    public Selector(List<Element> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if ("".equals(elements.get(0).getCombinator().getValue())) {
            elements = new ArrayList<Element>(elements);
            elements.set(0, new Element(" ", elements.get(0).getValue()));
        }
        _elements = elements;
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        for (Element element : _elements) {
            // note: js implementation has a check for typeof(e) === 'string', but it's never true?
            element.printCSS(env, out);
        }
    }

    @Override
    public String toString() {
        return StringUtils.join(_elements, "");
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Selector", _elements);
    }
}
