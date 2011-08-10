package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.Importer;
import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;
import com.bazaarvoice.jless.parser.DebugPrinter;

//
// CSS @import node
//
public class ImportFile extends NodeWithPosition {

    private final Node _location;
    private final boolean _css;
    private final Node _root;

    public ImportFile(int position, Node location, Importer importer) {
        super(position);
        _location = location;

        String path = location.getStringValue();
        if (!(path.endsWith(".less") || path.endsWith(".css"))) {
            path = path + ".less";
        }
        _css = path.endsWith(".css") || importer == null;

        if (_css) {
            _root = null;
        } else {
            _root = importer.parseImport(path).resultValue;
        }
    }

    @Override
    public Node eval(Environment env) {
        if (_css) {
            return new ImportFile(getPosition(), _location.eval(env), null);
        } else {
            return _root.eval(env);
        }
    }

    @Override
    public void printCss(CssWriter out) {
        out.indent(this);
        out.print("@import ");
        out.print(_location);
        out.print(';');
        out.newline();
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("ImportFile", _location, _css, _root);
    }
}
