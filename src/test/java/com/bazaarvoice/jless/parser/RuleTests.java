package com.bazaarvoice.jless.parser;

import org.parboiled.BaseParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.matchers.SequenceMatcher;
import org.parboiled.parserunners.ReportingParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParseTreeUtils;
import org.parboiled.support.ParsingResult;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test
public class RuleTests {

    private final Parser _parser = Parboiled.createParser(Parser.class);

    public void testEntitesCall() {
        accept(_parser.EntitiesCall(), "%()");
        reject(_parser.EntitiesCall(), "url(image.gif)");
        accept(_parser.EntitiesCall(), "alpha(opacity=1)");
        accept(_parser.EntitiesCall(), "alpha(opacity=88 )");
        reject(_parser.EntitiesCall(), "alpha(opacity=1.0)");
        accept(_parser.EntitiesCall(), "alpha(opacity=@opacity)");
        accept(_parser.EntitiesCall(), "local(Futura-Medium)");
        accept(_parser.EntitiesCall(), "progid:DXImageTransform.Microsoft.gradient(startColorstr=@lightColor, endColorstr=@darkColor)");
    }

    public void testEntitiesKeyword() {
        accept(_parser.EntitiesKeyword(), "Futura-Medium");
    }

    public void testMixinCall() {
        accept(_parser.MixinCall(), ".mixin;");
    }

    public void testSelector() {
        accept(_parser.Selector(), "input[type=\"text\"].class#id[attr=32]:not(1)");
    }

    public void testAttribute() {
        accept(_parser.Attribute(), "[attr=32]");
    }

    public void testRuleSet() {
        accept(_parser.Ruleset(), "#id[attr=32] {}");
    }

    public void testRule() {
        accept(_parser.Rule(), "font: normal small/20px 'Trebuchet MS', Verdana, sans-serif;");
        accept(_parser.Rule(), "_color: blue;");
        accept(_parser.Rule(), "*color: blue; /* or #color: blue */;");
        accept(_parser.Rule(), "color/**/: blue;");
        accept(_parser.Rule(), "color: blue\\9;");
        accept(_parser.Rule(), "color/*\\**/: blue\\9;");
        accept(_parser.Rule(), "color: blue !ie;");
    }

    public void testProperty() {
        accept(_parser.Property(), "--");
        reject(_parser.Property(), "-");
    }

    public void testCssIdent() {
        accept(_parser.CssIdent(), "\\34 04");
    }

    public void testCssNum() {
        accept(_parser.CssNum(), "1");
        accept(_parser.CssNum(), "1234");
        accept(_parser.CssNum(), ".1234");
        accept(_parser.CssNum(), "123.1234");
        reject(_parser.CssNum(), "123.");
        reject(_parser.CssNum(), ".");
        reject(_parser.CssNum(), "");
    }

    public void testCssString() {
        accept(_parser.CssString(), "'abc'");
        accept(_parser.CssString(), "\"def\"");
        accept(_parser.CssString(), "'ab\\'c'");
        reject(_parser.CssString(), "'ab\\'c");
        reject(_parser.CssString(), "'ab\nc'");
    }

    public void testCssN1() {
        accept(_parser.CssNl(), "\r\n");
        reject(_parser.CssNl(), " ");
    }

    private void accept(Rule rule, String string) {
        ParsingResult<Void> result = new ReportingParseRunner<Void>(rule).run(string);
        Assert.assertTrue(result.matched, result.parseErrors.toString() + "\n" + string);
    }

    private void reject(Rule rule, String string) {
        rule = new SequenceMatcher(new Rule[]{rule, BaseParser.EOI});
        ParsingResult<?> result = new ReportingParseRunner<Void>(rule).run(string);
        Assert.assertFalse(result.matched, string);
    }

    private void debug(Rule rule, String string) {
        rule = new SequenceMatcher(new Rule[]{rule, BaseParser.EOI});
        ParsingResult<?> result = new TracingParseRunner<Void>(rule).run(string);
        System.out.println(ParseTreeUtils.printNodeTree(result));
        Assert.assertTrue(result.matched, string);
    }
}
