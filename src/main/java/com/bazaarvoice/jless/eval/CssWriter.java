package com.bazaarvoice.jless.eval;

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
        _inputBuffer = inputBuffer;
    }

    public boolean isCompressionEnabled() {
        return _compress;
    }

    public void beginScope() {
        _nesting++;
    }

    public void indent(NodeWithPosition node) {
        if (!_compress) {
            int line = _inputBuffer.getPosition(node.getPosition()).line;
            while (_numLines < line) {
                newline();
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
        int pos = -1;
        while ((pos = string.indexOf('\n', pos + 1)) != -1) {
            _numLines++;
        }
    }

    public void print(char ch) {
        _buf.append(ch);
        if (ch == '\n') {
            _numLines++;
        }
    }

    public void newline() {
        _buf.append('\n');
        _numLines++;
    }

    @Override
    public String toString() {
        return _buf.toString();
    }
}
