package com.bazaarvoice.jless.eval;

import com.bazaarvoice.jless.eval.annotations.Number;
import com.bazaarvoice.jless.eval.annotations.Value;
import com.bazaarvoice.jless.tree.Color;
import com.bazaarvoice.jless.tree.Dimension;
import com.bazaarvoice.jless.tree.Node;
import com.google.common.base.Function;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({"UnusedDeclaration"})
public class Functions {

    public static final Map<String, Function<List<Node>, Node>> MAP = new HashMap<String, Function<List<Node>, Node>>();
    static {
        // add all public methods
        for (Method method : Functions.class.getMethods()) {
            MAP.put(method.getName(), new FunctionImpl(method));
        }
    }

    public static Color rgb(@Number double r, @Number double g, @Number double b) {
        return rgba(r, g, b, 1.0);
    }

    public static Color rgba(@Number double r, @Number double g, @Number double b, @Number double a) {
        return new Color(r, g, b, a);
    }

    public static Color hsl(@Number double h, @Number double s, @Number double l) {
        return hsla(h, s, l, 1.0);
    }

    private static Color hsla(Color.HSL hsl) {
        return hsla(hsl.h, hsl.s, hsl.l, hsl.a);
    }

    public static Color hsla(@Number double h, @Number double s, @Number double l, @com.bazaarvoice.jless.eval.annotations.Number double a) {
        h = (h % 360) / 360;

        double m2 = l <= 0.5 ? l * (s + 1) : l + s - l * s;
        double m1 = l * 2 - m2;

        return new Color(
                hue(h + 1.0/3, m1, m2) * 255,
                hue(h        , m1, m2) * 255,
                hue(h - 1.0/3, m1, m2) * 255,
                a);
    }
    
    private static double hue(double h, double m1, double m2) {
        h = h < 0 ? h + 1 : (h > 1 ? h - 1 : h);
        if      (h * 6 < 1) return m1 + (m2 - m1) * h * 6;
        else if (h * 2 < 1) return m2;
        else if (h * 3 < 2) return m1 + (m2 - m1) * (2.0/3 - h) * 6;
        else                return m1;
    }

    public static Dimension hue(Color color) {
        return new Dimension(Math.round(color.toHSL().h));
    }

    public static Dimension saturation(Color color) {
        return new Dimension(Math.round(color.toHSL().s * 100), "%");
    }

    public static Dimension lightness(Color color) {
        return new Dimension(Math.round(color.toHSL().l * 100), "%");
    }

    public static Dimension alpha(Color color) {
        return new Dimension(color.getAlpha());
    }
    
    public static Color saturate(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.s += amount / 100;
        hsl.s = clamp(hsl.s);
        return hsla(hsl);
    }
    
    public static Color desaturate(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.s -= amount / 100;
        hsl.s = clamp(hsl.s);
        return hsla(hsl);
    }

    public static Color lighten(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.l += amount / 100;
        hsl.l = clamp(hsl.l);
        return hsla(hsl);
    }

    public static Color darken(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.l -= amount / 100;
        hsl.l = clamp(hsl.l);
        return hsla(hsl);
    }

    public static Color fadein(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.a += amount / 100;
        hsl.a = clamp(hsl.a);
        return hsla(hsl);
    }

    public static Color fadeout(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        hsl.a -= amount / 100;
        hsl.a = clamp(hsl.a);
        return hsla(hsl);
    }

    public static Color spin(Color color, @Value double amount) {
        Color.HSL hsl = color.toHSL();
        double hue = (hsl.h + amount) % 360;
        hsl.h = hue < 0 ? 360 + hue : hue;
        return hsla(hsl);
    }

    //
    // Copyright (c) 2006-2009 Hampton Catlin, Nathan Weizenbaum, and Chris Eppstein
    // http://sass-lang.com
    //
    public static Color mix(Color color1, Color color2, @Value double weight) {
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

    public static Color greyscale(Color color) {
        return desaturate(color, 100);
    }

//    public static Anonymous e(str) {
//        return new Anonymous(str instanceof tree.JavaScript ? str.evaluated : str);
//    }

//    public Anonymous escape(str) {
//        return new tree.Anonymous(encodeURI(str.value).replace(/=/g, "%3D").replace(/:/g, "%3A").replace(/#/g, "%23").replace(/;/g, "%3B").replace(/\(/g, "%28").replace(/\)/g, "%29"));
//    }

/*
    @Name("%")
    public static Quoted interpolate(List<Node> args) {
        String string = args.get(0).getValue().toString();
        for (int i = 1; i < args.size(); i++) {
            str = str.replace(/%[sda]/i, function(token) {
                var value = token.match(/s/i) ? args[i].value : args[i].toCSS();
                return token.match(/[A-Z]$/) ? encodeURIComponent(value) : value;
            });
        }
        string = StringUtils.replace(string, "%%", "%");
        return new Quoted('"', string, false);
    }
*/

    public static Dimension round(Dimension dim) {
        return new Dimension(Math.round(dim.getValue()), dim.getUnit());
    }

    private static double clamp(double val) {
        return Math.min(1, Math.max(0, val));
    }
}
