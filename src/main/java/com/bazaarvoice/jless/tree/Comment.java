package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

import java.util.List;

public class Comment extends NodeWithPosition {

    private final String _value;
    private final boolean _silent;
    private final boolean _block;

    public Comment(int position, String value, boolean block, boolean silent) {
        super(position);
        _value = value;
        _block = block;
        _silent = silent;
    }

    @Override
    public String getStringValue() {
        return _value;
    }

    @Override
    public void flatten(List<Selector> contexts, List<Node> parentBlock, List<Node> globalBlock) {
        if (!_silent) {
            parentBlock.add(this);
        }
    }

    @Override
    public void printCss(CssWriter out) {
        if (!out.isCompressionEnabled()) {
            if (_block) {
                out.indent(this);
            }
            out.print(_value);
            if (_block && !_silent) {
                out.newline();
            }
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Comment", _value, _silent);
    }
}