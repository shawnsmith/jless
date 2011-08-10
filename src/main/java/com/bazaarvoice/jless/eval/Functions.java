package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.eval.annotations.Less;
import com.bazaarvoice.jless.eval.annotations.Number;
import com.bazaarvoice.jless.eval.annotations.Value;
import com.bazaarvoice.jless.exception.VariableException;
import com.bazaarvoice.jless.tree.Anonymous;
import com.bazaarvoice.jless.tree.Color;
import com.bazaarvoice.jless.tree.Dimension;
import com.bazaarvoice.jless.tree.Node;
import com.bazaarvoice.jless.tree.Quoted;
import com.google.common.base.Function;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings({"UnusedDeclaration"})
public class Functions {

    public static final Map<String, Function<List<Node>, Node>> MAP = getLessFunctions(new Functions());

    /** Returns a map of all functions annotated with the @Less annotation. */
    public static Map<String, Function<List<Node>, Node>> getLessFunctions(Object... instances) {
        Map<String, Function<List<Node>, Node>> map = new HashMap<String, Function<List<Node>, Node>>();
        for (Object instance : instances) {
            for (Method method : instance.getClass().getMethods()) {
                FunctionImpl function = FunctionImpl.wrap(instance, method);
                if (function != null) {
                    Function<List<Node>, Node> old = map.put(function.getName(), function);
                    if (old != null) {
                        throw new IllegalStateException("Duplicate definitions of function " + function.getName() + ": " + old + " vs. " + function);
                    }
                }
            }
        }
        return map;
    }

    @Less
    public Color rgb(@Number double r, @Number double g, @Number double b) {
        return rgba(r, g, b, 1.0);
    }

    @Less
    public Color rgba(@Number double r, @Number double g, @Number double b, @Number double a) {
        return new Color(r, g, b, a);
    }

    @Less
    public Color hsl(@Number double h, @Number double s, @Number double l) {
        return hsla(h, s, l, 1.0);
    }

    private Color hsla(Color.HSL hsl) {
        return hsla(hsl.h, hsl.s, hsl.l, hsl.a);
    }

    @Less
    public Color hsla(@Number double h, @Number double s, @Number double l, @com.bazaarvoice.jless.eval.annotations.Number double a) {
        h = (h % 360) / 360;

        double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
        double m1 = l * 2 - m2;

        return new Color(
                hue(h + 1.0/3, m1, m2) * 255,
                hue(h        , m1, m2) * 255,
                hue(h - 1.0/3, m1, m2) * 255,
                a);
    }

    private double hue(double h, double m1, double m2) {
        h = h < 0 ? h + 1 : (h > 1 ? h - 1 : h);
        if      (h * 6 < 1) return m1 + (m2 - m1) * h * 6;
        else if (h * 2 < 1) return m2;
        else if (h * 3 < 2) return m1 + (m2 - m1) * (2.0/3 - h) * 6;
        else                return m1;
    }

    @Less
    public Dimension hue(Color color) {
        return new Dimension(Math.round(color.toHSL().h));
    }

    @Less
    public Dimension saturation(Color color) {
        return new Dimension(Math.round(color.toHSL().s * 100), "%");
    }

    @Less
    public Dimension lightness(Color color) {
        return new Dimension(Math.round(color.toHSL().l * 100), "%");
    }

    @Less
    public Dimension alpha(Color color) {
        return new Dimension(color.getAlpha());
    }

    @Less
    public Color saturate(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.s += amount / 100;
        hsl.s = clamp(hsl.s);
        return hsla(hsl);
    }

    @Less
    public Color desaturate(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.s -= amount / 100;
        hsl.s = clamp(hsl.s);
        return hsla(hsl);
    }

    @Less
    public Color lighten(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.l += amount / 100;
        hsl.l = clamp(hsl.l);
        return hsla(hsl);
    }

    @Less
    public Color darken(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.l -= amount / 100;
        hsl.l = clamp(hsl.l);
        return hsla(hsl);
    }

    @Less
    public Color fadein(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.a += amount / 100;
        hsl.a = clamp(hsl.a);
        return hsla(hsl);
    }

    @Less
    public Color fadeout(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.a -= amount / 100;
        hsl.a = clamp(hsl.a);
        return hsla(hsl);
    }

    @Less
    public Color spin(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        double hue = (hsl.h + amount) % 360;
        hsl.h = hue < 0 ? 360 + hue : hue;
        return hsla(hsl);
    }

    //
    // Copyright (c) 2006-2009 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein
    // http://sass-lang.com
    //
    @Less
    public Color mix(Color color1, Color color2, @Value double weight) {
        double p = weight / 100.0;
        double w = p * 2 - 1;
        double a = color1.toHSL().a - color2.toHSL().a;

        double w1 = (((w * a == -1) ? w : (w + a) / (1 + w * a)) + 1) / 2.0;
        double w2 = 1 - w1;

        double[] rgb1 = color1.getRgb();
        double[] rgb2 = color2.getRgb();
        return new Color(
                rgb1[0] * w1 + rgb2[0] * w2,
                rgb1[1] * w1 + rgb2[1] * w2,
                rgb1[2] * w1 + rgb2[2] * w2,
                color1.getAlpha() * p + color2.getAlpha() * (1 - p)
        );
    }

    @Less
    public Color greyscale(Color color) {
        return desaturate(color, 100);
    }

    @Less
    public Anonymous e(Node node) {
        return new Anonymous(node.getStringValue());
    }

    @Less
    public Anonymous escape(Node node) {
        return new Anonymous(encodeURIComponent(node.getStringValue()));
    }

    private static final Pattern INTERPOLATION = Pattern.compile("%[sda]", Pattern.CASE_INSENSITIVE);

    @Less(name="%")
    public Quoted interpolate(Node node, Node... args) {
        String string = node.getStringValue();

        StringBuffer buf = new StringBuffer();
        Matcher matcher = INTERPOLATION.matcher(string);
        for (Node arg : args) {
            if (!matcher.find()) {
                break;
            }
            char token = matcher.group(0).charAt(1);
            String value = token == 's' || token == 'S' ? arg.getStringValue() : arg.toString();
            matcher.appendReplacement(buf, 'A' <= token && token <= 'Z' ? encodeURIComponent(value) : value);
        }
        matcher.appendTail(buf);
        string = buf.toString();

        string = StringUtils.replace(string, "%%", "%");
        return new Quoted('"', string, false);
    }

    @Less
    public Dimension round(Dimension dim) {
        return new Dimension(Math.round(dim.getValue()), dim.getUnit());
    }

    private double clamp(double val) {
        return Math.min(1, Math.max(0, val));
    }

    private String encodeURIComponent(String string) {
        try {
            return URLEncoder.encode(string, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);  // UTF-8 should always be supported
        }
    }
}
