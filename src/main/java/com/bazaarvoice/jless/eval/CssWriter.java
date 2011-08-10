package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.NodeWithPosition;
import org.parboiled.buffers.InputBuffer;

public class CssWriter {

    private final StringBuilder _buf = new StringBuilder();
    private final boolean _compress;
    private final InputBuffer _inputBuffer;
    private int _numLines;
    private int _nesting;

    public CssWriter() {
        this(true, null);
    }

    public CssWriter(boolean compress, InputBuffer inputBuffer) {
        _compress = compress;
        _inputBuffer = compress ? null : inputBuffer;  // the input buffer is ignored if compressing whitespace
    }

    public CssWriter subWriter() {
        return new CssWriter(_compress, null);  // preserve the compress setting but don't try to preserve line numbers
    }

    public boolean isCompressionEnabled() {
        return _compress;
    }

    public void beginScope() {
        _nesting++;
    }

    public void indent(NodeWithPosition node) {
        if (!_compress) {
            if (_inputBuffer != null) {
                int line = _inputBuffer.getPosition(node.getPosition()).line;
                while (_numLines < line - 1) {
                    newline();
                }
            }
            for (int i = 0; i < _nesting; i++) {
                _buf.append(' ').append(' ');
            }
        }
    }

    public void endScope() {
        _nesting--;
    }

    public void print(String string) {
        _buf.append(string);
        if (!_compress) {
            for (int pos = 0; (pos = string.indexOf('\n', pos)) != -1; pos++) {
                _numLines++;
            }
        }
    }

    public void print(char ch) {
        _buf.append(ch);
        if (!_compress && ch == '\n') {
            _numLines++;
        }
    }

    public void print(Node node) {
        node.printCSS(this);
    }

    public void print(Iterable<? extends Node> nodes, String compressedSeparator, String uncompressedSeparator) {
        String separator = "";
        String nextSeparator = _compress ? compressedSeparator : uncompressedSeparator;
        for (Node node : nodes) {
            print(separator);
            node.printCSS(this);
            separator = nextSeparator;
        }
    }

    public void newline() {
        if (!_compress) {
            _buf.append('\n');
            _numLines++;
        }
    }

    @Override
    public String toString() {
        return _buf.toString();
    }
}
