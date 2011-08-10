package com.bazaarvoice.jless.exception;

import org.parboiled.errors.ErrorUtils;
import org.parboiled.errors.ParserRuntimeException;
import org.parboiled.support.ParsingResult;

import java.net.URI;

public class ParseException extends ParserRuntimeException {

    public ParseException(String message) {
        super(message);
    }

    public ParseException(String message, Throwable t) {
        super(message, t);
    }

    public ParseException(URI uri, ParsingResult<?> parsingResult) {
        this("An error occurred while parsing a LESS stylesheet: " + uri + "\n" + ErrorUtils.printParseErrors(parsingResult));
    }
}
