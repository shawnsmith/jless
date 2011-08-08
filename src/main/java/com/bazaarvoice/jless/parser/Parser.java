/**
 * Copyright 2010 Bazaarvoice, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author J. Ryan Stinnett (ryan.stinnett@bazaarvoice.com)
 * @author Shawn Smith (shawn.smith@bazaarvoice.com)
 */

package com.bazaarvoice.jless.parser;

import com.bazaarvoice.jless.tree.Alpha;
import com.bazaarvoice.jless.tree.Anonymous;
import com.bazaarvoice.jless.tree.Block;
import com.bazaarvoice.jless.tree.Call;
import com.bazaarvoice.jless.tree.Color;
import com.bazaarvoice.jless.tree.Combinator;
import com.bazaarvoice.jless.tree.Comment;
import com.bazaarvoice.jless.tree.DataUri;
import com.bazaarvoice.jless.tree.Dimension;
import com.bazaarvoice.jless.tree.Directive;
import com.bazaarvoice.jless.tree.Element;
import com.bazaarvoice.jless.tree.Expression;
import com.bazaarvoice.jless.tree.ImportFile;
import com.bazaarvoice.jless.tree.Keyword;
import com.bazaarvoice.jless.tree.MixinCall;
import com.bazaarvoice.jless.tree.MixinDefinition;
import com.bazaarvoice.jless.tree.MixinDefinitionParameter;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Operation;
import com.bazaarvoice.jless.tree.Progid;
import com.bazaarvoice.jless.tree.Quoted;
import com.bazaarvoice.jless.tree.Ruleset;
import com.bazaarvoice.jless.tree.Selector;
import com.bazaarvoice.jless.tree.Shorthand;
import com.bazaarvoice.jless.tree.Url;
import com.bazaarvoice.jless.tree.Value;
import com.bazaarvoice.jless.tree.Variable;
import org.parboiled.BaseParser;
import org.parboiled.Rule;
import org.parboiled.support.Var;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Initially transcribed into Parboiled from the
 * <a href="http://github.com/cloudhead/less/blob/master/lib/less/engine/grammar">LESS Treetop grammar</a>
 * that was created by Alexis Sellier as part of the LESS Ruby implementation.
 *
 * From there, the parser has been modified to more closely support Parboiled style and a modified set of requirements.
 * Parsing differences from LESS Ruby include:
 * <ul>
 *   <li>Selector parsing was rewritten using the <a href="http://www.w3.org/TR/css3-selectors/#grammar">CSS 3 selector grammar</a></li> 
 *   <li>Numbers in attribute selectors must be quoted (as per the CSS spec)</li>
 *   <li>Case-sensitivity was removed</li>
 *   <li>Line breaks can be used in all places where spaces are accepted</li>
 * </ul>
 *
 * This parser attempts to track line breaks so that the input and output files can have the same number of lines
 * (which is helpful when referencing styles via browser tools like Firebug).
 *
 * This list only notes changes in the <em>parsing</em> stage. See {@link com.bazaarvoice.jless.LessProcessor} for details on any changes
 * to the <em>translation</em> stage.
 *
 * @see com.bazaarvoice.jless.LessProcessor
 */
@SuppressWarnings( {"InfiniteRecursion"})
public class Parser extends BaseParser<Node> {

    // ********** Document **********

    public Rule Document() {
        Var<Integer> startIndex = new Var<Integer>();
        return Sequence(
                startIndex.set(currentIndex()),
                Primary(true),
                EOI,
                push(new Ruleset(startIndex.get(), (Block) pop()))
        );
    }

    //
    // The `primary` rule is the *entry* and *exit* point of the parser.
    // The rules here can appear at any level of the parse tree.
    //
    // The recursive nature of the grammar is an interplay between the `block`
    // rule, which represents `{ ... }`, the `ruleset` rule, and this `primary` rule,
    // as represented by this simplified grammar:
    //
    //     primary  →  (ruleset | rule)+
    //     ruleset  →  selector+ block
    //     block    →  '{' primary '}'
    //
    // Only at one point is the primary rule not called from the
    // block rule: at the root level.
    //
    Rule Primary(boolean root) {
        Var<Integer> startIndex = new Var<Integer>();
        Var<List<Node>> statements = new Var<List<Node>>();
        return Sequence(
                startIndex.set(currentIndex()),
                statements.set(new ArrayList<Node>()),
                ZeroOrMore(
                        FirstOf(
                                Sequence(
                                    FirstOf(
                                        MixinDefinition(),
                                        Rule(),
                                        Ruleset(),
                                        MixinCall(),
                                        Comment(),
                                        Directive()
                                    ), add(statements, pop())
                                ),
                                Spacing()
                        )
                ),
                push(new Block(startIndex.get(), root, statements.get()))
            );
    }

    // We create a Comment node for CSS comments `/* */`,
    // but keep the LeSS comments `//` silent, by just skipping
    // over them.
    Rule Comment() {
        return Sequence(
                Test('/'),  // for performance
                FirstOf(
                        // single line comment: '//' (!LB .)* LB Ws0
                        Sequence(
                                // javascript regular expression: /^\/\/.*/
                                Sequence("//", ZeroOrMore(TestNotEOL(), ANY), FirstOf('\n', "\r\n", '\r', EOI)),
                                push(new Comment(match(), true))
                        ),
                        // multiline comment: '/*' (!'*\/' .)* '*\/' Ws0
                        Sequence(
                                // javascript regular expression:   /^\/\*(?:[^*]|\*+[^\/*])*\*+\/\n?/
                                // note: css spec uses this regex:  /^\/\*[^*]*\*+([^/*][^*]*\*+)*\//
                                Sequence("/*", ZeroOrMore(TestNot("*/"), ANY), "*/"),
                                push(new Comment(match(), false))
                        )
                ),
                OptionalSpacing()
        );
    }

    //
    // A string, which supports escaping " and '
    //
    //     "milky way" 'he\'s the one!'
    //
    Rule EntitiesQuoted() {
        Var<Boolean> escaped = new Var<Boolean>(false);
        return Sequence(
                Optional('~', escaped.set(true)),
                CssString(),
                push(new Quoted(match(), escaped.get())),
                OptionalSpacing()
        );
    }

    //
    // A catch-all word, such as:
    //
    //     black border-collapse
    //
    Rule EntitiesKeyword() {
        // javascript regular expression: /^[A-Za-z-]+/
        return Sequence(
                CssIdent(),
                push(new Keyword(match())),
                OptionalSpacing()
        );
    }

    //
    // A function call
    //
    //     rgb(255, 0, 255)
    //
    // We also try to catch IE's `alpha()`, but let the `alpha` parser
    // deal with the details.
    //
    // The arguments are parsed with the `entities.arguments` parser.
    //
    Rule EntitiesCall() {
        Var<String> name = new Var<String>();
        Var<List<Expression>> arguments = new Var<List<Expression>>();
        return Sequence(
                // javascript regular expression: /^([\w-]+|%)\(/
                FirstOf(
                        Sequence("progid:", OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-."))),
                        CssIdent(),
                        '%'
                ), name.set(match()),
                !"url".equalsIgnoreCase(name.get()),
                FirstOf(
                        Sequence(
                                "alpha".equals(name.get()),
                                IEAlpha()
                        ),
                        Sequence(
                                "expression".equals(name.get()),
                                IEExpression()
                        ),
                        Sequence(
                                name.get().startsWith("progid:"),
                                IEProgId(name)
                        ),
                        Sequence(
                                !"alpha".equals(name.get()) &&
                                        !"expression".equals(name.get()) &&
                                        !name.get().startsWith("progid:"),
                                arguments.set(new ArrayList<Expression>()),
                                Terminal('('),
                                Optional(
                                        Expression(), add(arguments, (Expression) pop()),
                                        ZeroOrMore(
                                                Terminal(','),
                                                Expression(), add(arguments, (Expression) pop())
                                        )
                                ),
                                Terminal(')'),
                                push(new Call(name.get(), arguments.get()))
                        )
                )
        );
    }

    Rule EntitiesLiteral() {
        return FirstOf(
                EntitiesDimension(),
                EntitiesColor(),
                EntitiesQuoted()
        );
    }

    //
    // Parse url() tokens
    //
    // We use a specific rule for urls, because they don't really behave like
    // standard function calls. The difference is that the argument doesn't have
    // to be enclosed within a string, so it can't be parsed as an Expression.
    //
    Rule EntitiesUrl() {
        return Sequence(
                Terminal("url("),
                FirstOf(
                        EntitiesQuoted(),
                        EntitiesVariable(),
                        EntitiesDataUri(),
                        Sequence(
                                // javascript regular expression: /^[-\w%@$\/.&=:;#+?~]+/
                                OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-%@$/.&=:;#+?~")),
                                push(new Anonymous(match())),
                                OptionalSpacing()
                        )
                ),
                Terminal(')'),
                push(new Url(pop()))
        );
    }

    Rule EntitiesDataUri() {
        // note: this grammar won't strip comments/compress whitespace w/in data uri values
        Var<String> mime = new Var<String>();
        Var<String> charset = new Var<String>();
        Var<String> encoding = new Var<String>();
        Var<String> data = new Var<String>();
        return Sequence(
                Terminal("data:"),
                // javascript regular expression: /^[^\/]+\/[^,;)]+/
                Optional(OneOrMore(TestNot('/'), ANY), '/', OneOrMore(TestNot(AnyOf(",;)")), ANY)), mime.set(match()),
                // javascript regular expression: /^;\s*charset=[^,;)]+/
                Optional(';', OptionalSpacing(), "charset=", OneOrMore(TestNot(AnyOf(",;)")), ANY)), charset.set(match()),
                // javascript regular expression: /^;\s*base64/
                Optional(';', OptionalSpacing(), "base64", OptionalSpacing()), encoding.set(match()),
                // javascript regular expression: /^,\s*[^)]+/
                Sequence(',', OptionalSpacing(), OneOrMore(TestNot(')'), ANY)), data.set(match()),
                push(new DataUri(mime.get(), charset.get(), encoding.get(), data.get()))
        );
    }

    // A Variable entity, such as `@fink`, in
    //
    //     width: @fink + 2px
    //
    // We use a different parser for variable definitions,
    // see `parsers.variable`.
    //
    Rule EntitiesVariable() {
        return Sequence(
                // javascript regular expression: /^@@?[\w-]+/
                Sequence('@', Optional('@'), WordOrDashChars()),
                push(new Variable(match())),
                OptionalSpacing()
        );
    }

    //
    // A Hexadecimal color
    //
    //     #4F3C2F
    //
    // `rgb` and `hsl` colors are parsed through the `entities.call` parser.
    //
    Rule EntitiesColor() {
        return Sequence(
                // javascript regular expression: /^#([a-fA-F0-9]{6}|[a-fA-F0-9]{3})/
                '#',
                Sequence(HexChar(), HexChar(), HexChar(), Optional(HexChar(), HexChar(), HexChar())),
                push(new Color(match())),
                OptionalSpacing()
        );
    }

    //
    // A Dimension, that is, a number and a unit
    //
    //     0.5em 95%
    //
    Rule EntitiesDimension() {
        Var<String> number = new Var<String>();
        Var<String> unit = new Var<String>();
        return Sequence(
                // javascript regular expression: /^(-?\d*\.?\d+)(px|%|em|pc|ex|in|deg|s|ms|pt|cm|mm|rad|grad|turn)?/
                Sequence(Optional('-'), CssNum()), number.set(match()),
                Optional(
                        FirstOf(
                                "px", "%", "em", "pc", "ex", "in", "deg", "s", "ms", "pt", "cm", "mm", "rad", "grad", "turn"
                        )
                ), unit.set(match()),
                push(new Dimension(number.get(), unit.get())),
                OptionalSpacing()
        );
    }

    //
    // JavaScript code to be evaluated
    //
    //     `window.location.href`
    //
    Rule EntitiesJavaScript() {
        // note: JavaScript isn't supported
        return Sequence(
                Sequence(
                        Optional(Terminal('~')),
                        // javascript regular expression: /^`([^`]*)`/
                        '`', ZeroOrMore(Sequence(TestNot('`'), ANY)), '`'
                ),
                push(new Anonymous(match())),
                OptionalSpacing()
        );
    }

    //
    // The variable part of a variable definition. Used in the `rule` parser
    //
    //     @fink:
    //
    Rule Variable() {
        return Sequence(
                // javascript regular expression: /^(@[\w-]+)\s*:/
                Sequence('@', WordOrDashChars()),
                push(new Variable(match())),
                OptionalSpacing(),
                Terminal(':')
        );
    }

    //
    // A font size/line-height shorthand
    //
    //     small/12px
    //
    // We need to peek first, or we'll match on keywords and dimensions
    //
    Rule Shorthand() {
        return Sequence(
                // javascript regular expression: /^[\w@.%-]+\/[\w@.-]+/
                Test(
                        OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_@.%-")),
                        '/',
                        AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_@.-")
                ),
                Entity(),
                Terminal('/'),
                Entity(),
                swap(), push(new Shorthand(pop(), pop()))
        );
    }

    //
    // A Mixin call, with an optional argument list
    //
    //     #mixins > .square(#fff);
    //     .rounded(4px, black);
    //     .button;
    //
    // The `while` loop is there because mixins can be
    // namespaced, but we only support the child and descendant
    // selector for now.
    //
    Rule MixinCall() {
        Var<String> combinator = new Var<String>();
        Var<List<Element>> elements = new Var<List<Element>>();
        Var<List<Expression>> arguments = new Var<List<Expression>>();
        return Sequence(
                elements.set(new ArrayList<Element>()),
                OneOrMore(
                        // javascript regular expression: /^[#.](?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+/
                        Sequence(
                                Sequence(AnyOf(".#"), CssIdent()),
                                add(elements, new Element(combinator.get(), match())),
                                OptionalSpacing()
                        ),
                        Optional(Terminal('>')), combinator.set(match())
                ),
                arguments.set(new ArrayList<Expression>()),
                Optional(
                        Terminal('('),
                        Optional(
                                Expression(), add(arguments, (Expression) pop()),
                                ZeroOrMore(
                                        Terminal(','),
                                        Expression(), add(arguments, (Expression) pop())
                                )
                        ),
                        Terminal(')')
                ),
                FirstOf(Terminal(';'), Test('}')),
                push(new MixinCall(elements.get(), arguments.get()))
        );
    }

    //
    // A Mixin definition, with a list of parameters
    //
    //     .rounded (@radius: 2px, @color) {
    //        ...
    //     }
    //
    // Until we have a finer grained state-machine, we have to
    // do a look-ahead, to make sure we don't have a mixin call.
    // See the `rule` function for more information.
    //
    // We start by matching `.rounded (`, and then proceed on to
    // the argument list, which has optional default values.
    // We store the parameters in `params`, with a `value` key,
    // if there is a value, such as in the case of `@radius`.
    //
    // Once we've got our params list, and a closing `)`, we parse
    // the `{...}` block.
    //
    Rule MixinDefinition() {
        Var<String> name = new Var<String>();
        Var<List<MixinDefinitionParameter>> parameters = new Var<List<MixinDefinitionParameter>>();
        return Sequence(
                Sequence(
                    // javascript regular expression: /^([#.](?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+)\s*\(/
                    AnyOf("#."),
                    TestNot(Sequence(ZeroOrMore(TestNot('{'), ANY), FirstOf(';', '}'))), // /^[^{]*(;|})/
                    CssIdent()
                ),
                name.set(match()),
                OptionalSpacing(),
                Terminal('('),
                parameters.set(new ArrayList<MixinDefinitionParameter>()),
                Optional(
                        MixinDefinitionParameter(), add(parameters, (MixinDefinitionParameter) pop()),
                        ZeroOrMore(
                                Terminal(','),
                                MixinDefinitionParameter(), add(parameters, (MixinDefinitionParameter) pop())
                        )
                ),
                Terminal(')'),
                Block(),
                push(new MixinDefinition(name.get(), parameters.get(), (Block) pop()))
        );
    }

    Rule MixinDefinitionParameter() {
        Var<Variable> name = new Var<Variable>();
        Var<Node> value = new Var<Node>();
        return Sequence(
                FirstOf(
                        Sequence(
                                EntitiesVariable(), name.set((Variable) pop()),
                                Optional(
                                        Terminal(':'),
                                        Expression(), value.set(pop())
                                )
                        ),
                        Sequence(
                                EntitiesLiteral(), value.set(pop())
                        ),
                        Sequence(
                                EntitiesKeyword(), value.set(pop())
                        )
                ),
                push(new MixinDefinitionParameter(name.get(), value.get()))
        );
    }

    //
    // Entities are the smallest recognized token,
    // and can be found inside a rule's value.
    //
    Rule Entity() {
        return FirstOf(
                EntitiesLiteral(),
                EntitiesVariable(),
                EntitiesUrl(),
                EntitiesCall(),
                EntitiesKeyword(),
                EntitiesJavaScript(),
                Comment()
        );
    }

    //
    // A Rule terminator. Note that we use `peek()` to check for '}',
    // because the `block` rule will be expecting it, but we still need to make sure
    // it's there, if ';' was ommitted.
    //
    Rule End() {
        return FirstOf(Terminal(';'), Test('}'));
    }

    //
    // IE's alpha function
    //
    //     alpha(opacity=88)
    //
    Rule IEAlpha() {
        return Sequence(
                // javascript regular expression: /^\(opacity=/i
                Terminal('('), IgnoreCase("opacity"), OptionalSpacing(), Terminal('='),
                FirstOf(
                        Sequence(
                                CssNum(), push(new Keyword(match())),
                                OptionalSpacing()
                        ),
                        EntitiesVariable()
                ),
                Terminal(')'),
                push(new Alpha(pop()))
        );
    }

    //
    // IE's expression function
    //
    //     expression(document.body.scrollHeight + 'px')
    //
    Rule IEExpression() {
        // note: this is in jLess, but not Less
        return Sequence(
                BalancedParenthesis(),
                push(new Anonymous("expression" + match()))
        );
    }

    //
    // IE's progid function
    //
    //     progid:DXImageTransform.Microsoft.gradient(startColorstr=@lightColor, endColorstr=@darkColor)
    //
    Rule IEProgId(Var<String> name) {
        Var<String> key = new Var<String>();
        Var<Map<String, Node>> values = new Var<Map<String, Node>>();
        return Sequence(
                values.set(new LinkedHashMap<String, Node>()),
                '(',
                Optional(
                        CssIdent(), key.set(match()), Terminal('='), Expression(), put(values, key.get(), pop()),
                        ZeroOrMore(
                                Terminal(','),
                                CssIdent(), key.set(match()), Terminal('='), Expression(), put(values, key.get(), pop())
                        )
                ),
                Terminal(')'),
                push(new Progid(name.get(), values.get()))
        );
    }

    //
    // A Selector Element
    //
    //     div
    //     + h1
    //     #socks
    //     input[type="text"]
    //
    // Elements are the building blocks for Selectors,
    // they are made out of a `Combinator` (see combinator rule),
    // and an element name, such as a tag a class, or `*`.
    //
    Rule Element() {
        return FirstOf(
                Sequence(
                        Combinator(),
                        FirstOf(
                                // javascript regular expression: /^(?:[.#]?|:*)(?:[\w-]|\\(?:[a-fA-F0-9]{1,6} ?|[^a-fA-F0-9]))+/
                                Sequence(FirstOf('.', '#', ZeroOrMore(':')), CssIdent()),
                                '*',
                                Attribute(),
                                // javascript regular expression: /^\([^)@]+\)/
                                Sequence('(', OneOrMore(TestNot(AnyOf(")@")), ANY), ')'),
                                // javascript regular expression: /^(?:\d*\.)?\d+%/
                                Sequence(CssNum(), '%')
                        ),
                        push(new Element((Combinator) pop(), match())),
                        OptionalSpacing()
                ),
                Sequence(
                        // special case for '&' combinators where the selector is optional
                        FirstOf("& ", '&'),
                        push(new Element(match(), null)),
                        OptionalSpacing()
                )
        );
    }

    //
    // Combinators combine elements together, in a Selector.
    //
    // Because our parser isn't white-space sensitive, special care
    // has to be taken, when parsing the descendant combinator, ` `,
    // as it's an empty space. We have to check the previous character
    // in the input, to see if it's a ` ` character. More info on how
    // we deal with this in *combinator.js*.
    //
    Rule Combinator() {
        return FirstOf(
                Sequence(Sequence(AnyOf(">+~"), OptionalSpacing()), push(new Combinator(match()))),
                Sequence(Sequence(FirstOf("& ", '&'), OptionalSpacing()), push(new Combinator(match()))),
                Sequence(Sequence("::", OptionalSpacing()), push(new Combinator(match()))),
                Sequence(new LookBehindCharMatcher(' '), push(new Combinator(" "))),
                Sequence(EMPTY, push(new Combinator(null)))
        );
    }

    //
    // A CSS Selector
    //
    //     .class > div + h1
    //     li a:hover
    //
    // Selectors are made out of one or more Elements, see above.
    //
    Rule Selector() {
        Var<List<Element>> elements = new Var<List<Element>>();
        return Sequence(
                elements.set(new ArrayList<Element>()),
                Element(), add(elements, (Element) pop()),
                ZeroOrMore(
                        TestNot(AnyOf("{};,")),
                        Element(), add(elements, (Element) pop())
                ),
                push(new Selector(elements.get()))
        );
    }

    Rule Attribute() {
        return Sequence(
                Terminal('['),
                FirstOf(
                        // javascript regular expression: /^[a-zA-Z-]+/
                        Sequence(AlphaOrDashChars(), OptionalSpacing()),
                        EntitiesQuoted()
                ),
                Optional(
                        // javascript regular expression: /^[|~*$^]?=/
                        Optional(AnyOf("|~*$^")),
                        Terminal('='),
                        FirstOf(
                                EntitiesQuoted(),
                                Sequence(WordOrDashChars(), OptionalSpacing())
                        )
                ),
                Terminal(']')
        );
    }

    //
    // The `block` rule is used by `ruleset` and `mixin.definition`.
    // It's a wrapper around the `primary` rule, with added `{}`.
    //
    Rule Block() {
        return Sequence(
                Terminal('{'),
                Primary(false),
                Terminal('}')
        );
    }

    //
    // div, .class, body > p {...}
    //
    Rule Ruleset() {
        Var<Integer> startIndex = new Var<Integer>();
        Var<List<Selector>> selectors = new Var<List<Selector>>();
        return Sequence(
                startIndex.set(currentIndex()),
                FirstOf(
                        Sequence(
                                selectors.set(new ArrayList<Selector>()),
                                // javascript regular expression: /^([.#:% \w-]+)[\s\n]*\{/
                                OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.#:% ")),
                                add(selectors, new Selector(new Element("", match()))),
                                OptionalSpacing(),
                                Test('{')
                        ),
                        Sequence(
                                selectors.set(new ArrayList<Selector>()),
                                Selector(), add(selectors, (Selector) pop()),
                                ZeroOrMore(
                                        Terminal(','),
                                        Selector(), add(selectors, (Selector) pop())
                                        // todo: js grammar has explicit comment handling in here
                                )
                        )
                ),
                Block(),
                push(new Ruleset(startIndex.get(), selectors.get(), (Block) pop())),
                Optional(Terminal(';'))  // note: jLess allows this trailing semi-colon, Less does not.
        );
    }

    Rule Rule() {
        Var<Integer> startIndex = new Var<Integer>();
        Var<Node> name = new Var<Node>();
        Var<Boolean> important = new Var<Boolean>(false);
        return Sequence(
                startIndex.set(currentIndex()),
                TestNot(AnyOf(".#&")),
                FirstOf(Variable(), Property()), name.set(pop()),
                FirstOf(
                        Sequence(
                                !(name.get() instanceof Variable),
                                // javascript regular expression: /^([^@+\/'"*`(;{}-]*);/
                                ZeroOrMore(TestNot(AnyOf("@+\\/'\"*`(;{}-")), ANY),
                                push(new Anonymous(match())),
                                Test(';')
                        ),
                        Sequence(
                                "font".equals(name.get().toString()),
                                Font()
                        ),
                        Sequence(
                                !"font".equals(name.get().toString()),
                                Value()
                        )
                ),
                Optional(Important(), important.set(true)),
                End(),
                push(new com.bazaarvoice.jless.tree.Rule(startIndex.get(), name.get(), pop(), important.get()))
        );
    }

    //
    // An @import directive
    //
    //     @import "lib";
    //
    // Depending on our environemnt, importing is done differently:
    // In the browser, it's an XHR request, in Node, it would be a
    // file-system operation. The function used for importing is
    // stored in `import`, which we pass to the Import constructor.
    //
    Rule ImportFile() {
        Var<Integer> startIndex = new Var<Integer>();
        return Sequence(
                startIndex.set(currentIndex()),
                "@import",
                Spacing(),
                FirstOf(EntitiesQuoted(), EntitiesUrl()),
                Terminal(';'),
                push(new ImportFile(startIndex.get(), pop()))
        );
    }

    //
    // A CSS Directive
    //
    //     @charset "utf-8";
    //
    Rule Directive() {
        Var<Integer> startIndex = new Var<Integer>();
        Var<String> name = new Var<String>();
        Var<String> types = new Var<String>();
        return Sequence(
                startIndex.set(currentIndex()),
                Test('@'),
                FirstOf(
                        ImportFile(),
                        Sequence(
                                FirstOf("@media", "@page", "@-webkit-keyframes", "@keyframes"), name.set(match()),
                                TestNot(AlphaOrDashChars()), // js less doesn't appear to require anything between the name and types, bug?
                                OptionalSpacing(),
                                // javascript regular expression: /^[^{]+/
                                ZeroOrMore(TestNot('{'), ANY), types.set(match().trim()),
                                Block(),
                                push(new Directive(startIndex.get(), name.get() + " " + types.get(), (Block) pop()))
                        ),
                        Sequence(
                                '@', AlphaOrDashChars(), name.set("@" + match()),
                                OptionalSpacing(),
                                FirstOf(
                                        Sequence(
                                                "@font-face".equals(name.get()),
                                                Block(),
                                                push(new Directive(startIndex.get(), name.get(), (Block) pop()))
                                        ),
                                        Sequence(
                                                !"@font-face".equals(name.get()),
                                                Entity(),
                                                Terminal(';'),
                                                push(new Directive(startIndex.get(), name.get(), pop()))
                                        )
                                )
                        )
                )
        );
    }
    Rule Font() {
        Var<List<Node>> values = new Var<List<Node>>();
        Var<List<Node>> expressions = new Var<List<Node>>();
        return Sequence(
                values.set(new ArrayList<Node>()),

                expressions.set(new ArrayList<Node>()),
                ZeroOrMore(
                        FirstOf(
                                Shorthand(),
                                Entity()
                        ), add(expressions, pop())
                ),
                add(values, new Expression(expressions.get())),

                ZeroOrMore(
                        Terminal(','),
                        Expression(), add(values, pop())
                ),
                push(new Value(values.get()))
        );
    }

    //
    // A Value is a comma-delimited list of Expressions
    //
    //     font-family: Baskerville, Georgia, serif;
    //
    // In a Rule, a Value represents everything after the `:`,
    // and before the `;`.
    //
    Rule Value() {
        Var<List<Node>> expressions = new Var<List<Node>>();
        return Sequence(
                expressions.set(new ArrayList<Node>()),
                Expression(), add(expressions, pop()),
                ZeroOrMore(
                        Terminal(','),
                        Expression(), add(expressions, pop())
                ),
                push(new Value(expressions.get()))
        );
    }

    Rule Important() {
        return Sequence('!', OptionalWhitespace(), "important", OptionalSpacing());
    }

    Rule Sub() {
        return Sequence(Terminal('('), Expression(), Terminal(')'));
    }

    Rule Multiplication() {
        Var<Character> op = new Var<Character>();
        return Sequence(
                Operand(),
                ZeroOrMore(
                        AnyOf("*/"), op.set(matchedChar()),
                        Optional(Sequence(Whitespace(), OptionalSpacing())),  // comments must be separated from the operator by a space
                        Operand(),
                        swap(), push(new Operation(op.get(), pop(), pop()))
                )
        );
    }

    Rule Addition() {
        Var<Character> op = new Var<Character>();
        return Sequence(
                Multiplication(),
                ZeroOrMore(
                        FirstOf(
                                Sequence(
                                        AnyOf("-+"), op.set(matchedChar()),
                                        Spacing()
                                ),
                                Sequence(
                                        TestNot(new LookBehindCharMatcher(' ')),
                                        AnyOf("-+"), op.set(matchedChar())
                                )
                        ),
                        Multiplication(),
                        swap(), push(new Operation(op.get(), pop(), pop()))
                )
        );
    }

    //
    // An operand is anything that can be part of an operation,
    // such as a Color, or a Variable
    //
    Rule Operand() {
        Var<Boolean> negate = new Var<Boolean>(false);
        return Sequence(
                Optional('-', Test(AnyOf("@(")), negate.set(true)),
                FirstOf(
                        Sub(),
                        EntitiesDimension(),
                        EntitiesColor(),
                        EntitiesVariable(),
                        EntitiesCall()
                ),
                negate.get() && push(new Operation('*', new Dimension(-1, null), pop())) || true
        );
    }

    //
    // Expressions either represent mathematical operations,
    // or white-space delimited Entities.
    //
    //     1px solid black
    //     @var * 2
    //
    Rule Expression() {
        Var<List<Node>> values = new Var<List<Node>>();
        return Sequence(
                values.set(new ArrayList<Node>()),
                OneOrMore(
                        FirstOf(
                                Addition(),
                                Entity()
                        ), add(values, pop())
                ),
                push(new Expression(values.get()))
        );
    }

    Rule Property() {
        return Sequence(
                // javascript regular expression: /^(\*?-?[-a-z_0-9]+)\s*:/
                Sequence(Optional('*'), CssIdent()),
                push(new Keyword(match())),
                OptionalSpacing(),
                Terminal(':')
        );
    }

    // ********** Rules that Don't Modify the Value Stack **********

    /** Equivalent to regular expression '[a-zA-Z-]+' */
    Rule AlphaOrDashChars() {
        return OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ-"));
    }

    /** Equivalent to regular expression '[\w-]+' */
    Rule WordOrDashChars() {
        return OneOrMore(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-"));
    }

    /** Equivalent to regular expression '[a-fA-F0-9]' */
    Rule HexChar() {
        return AnyOf("ABCDEFabcdef0123456789");
    }

    /** Equivalent to regular expression '\d' */
    Rule DigitChar() {
        return CharRange('0', '9');
    }

    Rule Terminal(char ch) {
        return Sequence(ch, OptionalSpacing());
    }

    Rule Terminal(String string) {
        return Sequence(string, OptionalSpacing());
    }

    Rule Spacing() {
        return Sequence(
                Test(AnyOf(" \t\r\n\f/")),  // for performance
                OneOrMore(
                        FirstOf(
                                Whitespace(),
                                Sequence(Comment(), drop())  // todo: this throws away the comment...
                        )
                )
        );
    }

    Rule OptionalSpacing() {
        return Optional(Spacing());
    }

    // whitespace, as defined by the CSS specification
    Rule Whitespace() {
        return OneOrMore(AnyOf(" \t\r\n\f"));
    }

    Rule OptionalWhitespace() {
        return ZeroOrMore(AnyOf(" \t\r\n\f"));
    }

    Rule TestNotEOL() {
        return TestNot(AnyOf("\r\n"));
    }

    Rule BalancedParenthesis() {
        return Sequence(
                '(',
                ZeroOrMore(
                        FirstOf(
                                OneOrMore(TestNot(AnyOf("()")), ANY),
                                BalancedParenthesis()
                        )
                ),
                Terminal(')')
        );
    }

    // ********** Token Definitions from the CSS Specification **********

    // ident    [-]?{nmstart}{nmchar}*
    Rule CssIdent() {
        return Sequence(Optional('-'), CssNmstart(), OptionalCssNmchars());
    }

    // nmstart  [_a-z]|{nonascii}|{escape}
    Rule CssNmstart() {
        return FirstOf(AnyOf("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_"), CssNonascii(), CssEscape());
    }

    // nmchar   [_a-z0-9-]|{nonascii}|{escape}
    Rule OptionalCssNmchars() {
        return ZeroOrMore(FirstOf(WordOrDashChars(), CssNonascii(), CssEscape()));
    }

    // nonascii [^\0-\237]
    Rule CssNonascii() {
        return Sequence(TestNot(CharRange((char) 0, (char) 237)), ANY);
    }

    // escape   {unicode}|\\[^\n\r\f0-9a-f]
    Rule CssEscape() {
        return Sequence(
                Test('\\'),  // for performance
                FirstOf(
                        CssUnicode(),
                        Sequence('\\', Sequence(TestNot("\n\r\f"), ANY))
                )
        );
    }

    // unicode  \\[0-9a-f]{1,6}(\r\n|[ \n\r\t\f])?
    Rule CssUnicode() {
        return Sequence(
                '\\',
                HexChar(), Optional(HexChar(), Optional(HexChar(), Optional(HexChar(), Optional(HexChar(), Optional(HexChar()))))),
                FirstOf("\r\n", AnyOf(" \n\r\t\f"), EMPTY)
        );
    }

    // num    [0-9]+|[0-9]*\.[0-9]+
    Rule CssNum() {
        return FirstOf(
                Sequence(OneOrMore(DigitChar()), Optional('.', OneOrMore(DigitChar()))),
                Sequence('.', OneOrMore(DigitChar()))
        );
    }

    // string   {string1}|{string2}
    Rule CssString() {
        return FirstOf(
                // string1   \"([^\n\r\f\\"]|\\{nl}|{escape})*\"
                Sequence(
                        '"',
                        ZeroOrMore(
                                FirstOf(
                                        OneOrMore(Sequence(TestNot(AnyOf("\n\r\f\\\"")), ANY)),
                                        Sequence('\\', CssNl()),
                                        CssEscape()
                                )
                        ),
                        '"'
                ),
                // string2   \'([^\n\r\f\\']|\\{nl}|{escape})*\'
                Sequence(
                        '\'',
                        ZeroOrMore(
                                FirstOf(
                                        OneOrMore(Sequence(TestNot(AnyOf("\n\r\f\\'")), ANY)),
                                        Sequence('\\', CssNl()),
                                        CssEscape()
                                )
                        ),
                        '\''
                )
        );
    }

    // nl   \n|\r\n|\r|\f
    Rule CssNl() {
        return FirstOf('\n', "\r\n", '\r', '\f');
    }

    <T> boolean add(Var<List<T>> list, T obj) {
        list.get().add(obj);
        return true;
    }

    <K,V> boolean put(Var<Map<K,V>> map, K key, V value) {
        map.get().put(key, value);
        return true;
    }
}
