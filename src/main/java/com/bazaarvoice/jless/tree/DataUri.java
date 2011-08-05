package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.parser.DebugPrinter;

public class DataUri extends Node {

    private final String _mime;
    private final String _charset;
    private final String _encoding;
    private final String _data;

    public DataUri(String mime, String charset, String encoding, String data) {
        _mime = mime;
        _charset = charset;
        _encoding = encoding;
        _data = data;
    }

    @Override
    public String toString() {
        return "data:" + _mime + _charset + _encoding + _data;
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Data", _mime, _charset, _encoding, _data);
    }
}
