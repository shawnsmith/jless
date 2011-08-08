package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;
import org.parboiled.support.Position;

//
// CSS @import node
//
public class ImportFile extends NodeWithPosition {

    private final String _path;
    private final boolean _css;

    public ImportFile(int position, Node location) {
        super(position);
        String path;
        if (location instanceof Quoted) {
            path = ((Quoted) location).getValue();
            if (!(path.endsWith(".less") || path.endsWith(".css"))) {
                path = path + ".less";
            }
        } else if (location instanceof Url) {
            path = location.toString();
        } else {
            throw new IllegalArgumentException(location.toString());
        }
        _path = path;
        _css = path.endsWith(".css");
    }

    @Override
    public void printCSS(Environment env, CssWriter out) {
        if (_css) {
            out.indent(this);
            out.print(toString());
            out.newline();
        } else {
            // todo: evaluate the less file referenced by the import
        }
    }

    @Override
    public String toString() {
        return "@import " + _path + ";\n";
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("ImportFile", _path, _css);
    }
}
