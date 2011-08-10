package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

//
// CSS @import node
//
public class ImportFile extends NodeWithPosition {

    private final Node _location;
    private final String _path;
    private final boolean _css;

    public ImportFile(int position, Node location) {
        super(position);
        _location = location;
        String path;
        if (location instanceof Quoted) {
            path = ((Quoted) location).getStringValue();
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
    public Node eval(Environment env) {
        if (_css) {
            return new ImportFile(getPosition(), _location.eval(env));
        } else {
            // todo: evaluate the less file referenced by the import
//            throw new UnsupportedOperationException();
            return null;
        }
    }

    @Override
    public void printCSS(CssWriter out) {
        out.indent(this);
        out.print("@import ");
        out.print(_location);
        out.print(';');
        out.newline();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("ImportFile", _path, _css);
    }
}
