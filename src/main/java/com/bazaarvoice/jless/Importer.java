package com.bazaarvoice.jless;

import com.bazaarvoice.jless.exception.ParseException;
import com.bazaarvoice.jless.parser.Parser;
import com.bazaarvoice.jless.parser.ParserFactory;
import com.bazaarvoice.jless.tree.Node;
import org.parboiled.parserunners.ParseRunner;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.support.ParsingResult;

import java.io.File;
import java.net.URI;

public class Importer {

    private final Loader _loader;
    private final URI _baseUri;

    public Importer(File file) {
        this(new UrlLoader(), file.toURI());
    }

    public Importer(Loader loader, URI baseUri) {
        _loader = loader;
        _baseUri = baseUri;
    }

    public ParsingResult<Node> parse() {
        return parseString(_loader.load(_baseUri));
    }

    public ParsingResult<Node> parseImport(String path) {
        URI uri = _baseUri.resolve(path);
        return new Importer(_loader, uri).parse();
    }

    public ParsingResult<Node> parseString(String string) {
        Parser parser = ParserFactory.newInstance(this);
        ParseRunner<Node> parseRunner = new ReportingParseRunner<Node>(parser.Document());
        //ParseRunner<Node> parseRunner = new TracingParseRunner<Node>(parser.Document());
        ParsingResult<Node> result = parseRunner.run(string);
        if (result.hasErrors()) {
            throw new ParseException(_baseUri, result);
        }
        return result;
    }
}
