package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

//
// CSS @import node
//
public class ImportFile extends Node {

    private final String _path;
    private final boolean _css;

    public ImportFile(Node location) {
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
    public String toCSS(Environment env) {
        if (_css) {
            return toString();
        }
        // todo: evaluate the less file referenced by the import
        return "";
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
