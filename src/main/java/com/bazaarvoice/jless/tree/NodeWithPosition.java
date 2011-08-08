package com.bazaarvoice.jless.tree;

public abstract class NodeWithPosition extends Node {

    private final int _position;

    protected NodeWithPosition(int position) {
        _position = position;
    }

    public int getPosition() {
        return _position;
    }
}
