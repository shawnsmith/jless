package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.exception.VariableException;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Quoted extends Node {
    private static final Pattern INTERPOLATION = Pattern.compile("@\\{([\\w-]+)\\}");

    private final char _quote;
    private final String _value;
    private final boolean _escaped;

    public Quoted(String string, boolean escaped) {
        this(string.charAt(0), string.substring(1, string.length() - 1), escaped);
    }

    public Quoted(char quote, String value, boolean escaped) {
        _quote = quote;
        _value = value;
        _escaped = escaped;
    }

    @Override
    public String getValue() {
        return _value;
    }

    @Override
    public Node eval(Environment env) {
        Matcher matcher = INTERPOLATION.matcher(_value);
        if (matcher.find()) {
            StringBuffer buf = new StringBuffer();
            do {
                String name = "@" + matcher.group(1);
                Node value = env.getVariable(name);
                if (value == null) {
                    throw new VariableException("Variable is undefined: " + name);
                }
                matcher.appendReplacement(buf, value.toString());
            } while (matcher.find());
            matcher.appendTail(buf);
            return new Quoted(_quote, buf.toString(), _escaped);
        }
        return this;
    }

    @Override
    public void printCSS(CssWriter out) {
        if (_escaped) {
            out.print(_value);
        } else {
            out.print(_quote);
            out.print(_value);
            out.print(_quote);
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Quoted", _quote + _value + _quote, _escaped);
    }
}
