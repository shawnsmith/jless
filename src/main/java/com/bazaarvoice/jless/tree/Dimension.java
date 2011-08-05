package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.parser.DebugPrinter;

//
// A number with a unit
//
public class Dimension extends Node {

    private final double _value;
    private final String _unit;

    public Dimension(String value, String unit) {
        this(Double.parseDouble(value), unit);
    }

    public Dimension(double value, String unit) {
        _value = value;
        _unit = unit != null ? unit : "";
    }

/*
    @Override
    public Color toColor() {
        return new Color(new double[]{_value, _value, _value}, 1);
    }
*/

    @Override
    public String toString() {
        String string = Double.toString(_value);
        if (string.endsWith(".0")) {
            string = string.substring(0, string.length() - 2);
        }
        return string + _unit;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Dimension", _value, _unit);
    }

    // In an operation between two Dimensions,
    // we default to the first Dimension's unit,
    // so `1px + 2em` will yield `3px`.
    // In the future, we could implement some unit
    // conversions such that `100cm + 10mm` would yield
    // `101cm`.
/*
    @Override
    public Node operate(char op, Node other) {
        return new Dimension(operate(op, _value, other._value),
                StringUtils.isNotEmpty(_unit) ? _unit : other._unit);
    }
*/
}
