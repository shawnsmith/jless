package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.eval.Environment;

public interface Document {

    Document eval(Environment env);

    Document flatten();

    void printCss(CssWriter out);

    String toString();
}
