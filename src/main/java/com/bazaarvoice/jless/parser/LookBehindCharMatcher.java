package com.bazaarvoice.jless.parser;

import org.parboiled.MatcherContext;
import org.parboiled.common.StringUtils;
import org.parboiled.matchers.CustomMatcher;

public class LookBehindCharMatcher extends CustomMatcher {

    private final char _character;

    public LookBehindCharMatcher(char character) {
        super("(?<=" + StringUtils.escape(character) + ")");  // regex positive lookbehind-style label
        _character = character;
    }

    @Override
    public boolean isSingleCharMatcher() {
        return false;
    }

    @Override
    public boolean canMatchEmpty() {
        return true;
    }

    @Override
    public boolean isStarterChar(char c) {
        return false;
    }

    @Override
    public char getStarterChar() {
        return 0;
    }

    @Override
    public <V> boolean match(MatcherContext<V> context) {
        int idx = context.getCurrentIndex();
        return idx >= 0 && context.getInputBuffer().charAt(idx - 1) == _character;
    }
}
