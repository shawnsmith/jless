package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Selector extends Node {

    private final List<Element> _elements;

    public Selector(boolean root) {
        if (!root) {
            throw new IllegalArgumentException();
        }
        _elements = Collections.emptyList();
    }

    public Selector(Element element) {
        this(Collections.singletonList(element));
    }

    public Selector(List<Element> elements) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException();
        }
        if ("".equals(elements.get(0).getCombinator().getStringValue())) {
            elements = new ArrayList<Element>(elements);
            elements.set(0, new Element(" ", elements.get(0).getStringValue()));
        }
        _elements = elements;
    }

    public List<Element> getElements() {
        return _elements;
    }

    @Override
    public void printCss(CssWriter out) {
        for (Element element : _elements) {
            // note: js implementation has a check for typeof(e) === 'string', but it's never true?
            out.print(element);
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Selector", _elements);
    }
}
