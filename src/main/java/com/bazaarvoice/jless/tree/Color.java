package com.bazaarvoice.jless.tree;

import com.bazaarvoice.jless.eval.CssWriter;
import com.bazaarvoice.jless.parser.DebugPrinter;

//
// RGB Colors - #ff0014, #eee
//
public class Color extends Node {

    private final double[] _rgb;
    private final double _alpha;

    public Color(double r, double g, double b, double alpha) {
        this(new double[]{r, g, b}, alpha);
    }

    public Color(double[] rgb, double alpha) {
        _rgb = rgb;
        _alpha = alpha;
    }

    public Color(String rgb) {
        this(rgb, 1);
    }

    public Color(String rgb, double alpha) {
        //
        // The end goal here, is to parse the arguments
        // into an integer triplet, such as `128, 255, 0`
        //
        // This facilitates operations and conversions.
        //
        if (rgb.length() == 8) {
            // ##aaffbbcc
            alpha = Integer.parseInt(rgb.substring(0, 2), 16) / 255.0;
            rgb = rgb.substring(2);
        }
        _rgb = new double[3];
        if (rgb.length() == 6) {
            // #ffbbcc
            for (int i = 0; i < 3; i++) {
                _rgb[i] = Integer.parseInt(rgb.substring(i * 2, i * 2 + 2), 16);
            }
        } else {
            // #fbc
            for (int i = 0; i < Math.min(3, rgb.length()); i++) {
                _rgb[i] = Integer.parseInt(rgb.substring(i, i + 1) + rgb.charAt(i), 16);
            }
        }
        _alpha = alpha;
    }

    @Override
    public Color toColor() {
        return this;
    }

    public double[] getRgb() {
        return _rgb;
    }

    public double getAlpha() {
        return _alpha;
    }

    //
    // If we have some transparency, the only way to represent it
    // is via `rgba`. Otherwise, we use the hex representation,
    // which has better compatibility with older browsers.
    // Values are capped between `0` and `255`, rounded and zero-padded.
    //
    @Override
    public void printCSS(CssWriter out) {
        if (_alpha < 1.0) {
            // has alpha, must use rgba(r,g,b,a) function
            out.print("rgba(");
            for (double c : _rgb) {
                out.print(Long.toString(Math.round(c)));
                out.print(',');
            }
            out.print(Double.toString(_alpha));
            out.print(')');
        } else {
            // opaque so use the more compact #rrggbb syntax
            out.print('#');
            int n = 0;
            for (double c : _rgb) {
                long i = Math.round(c);
                n = (n << 8) | (int) Math.max(0, Math.min(i, 255));
            }
            String s = Integer.toHexString(n);
            for (int i = s.length(); i < 6; i++) {
                out.print('0');
            }
            out.print(s);
        }
    }

    @Override
    public DebugPrinter toDebugPrinter() {
        return new DebugPrinter("Color", _rgb[0], _rgb[1], _rgb[2], _alpha);
    }

    //
    // Operations have to be done per-channel, if not,
    // channels will spill onto each other. Once we have
    // our result, in the form of an integer triplet,
    // we create a new Color node to hold the result.
    //
    public Node operate(char op, Node other) {
        Color color = other.toColor();
        double[] result = new double[3];
        for (int i = 0; i < 3; i++) {
            result[i] = operate(op, _rgb[i], color._rgb[i]);
        }
        return new Color(result, _alpha + color._alpha);
    }

    public HSL toHSL() {
        double r = _rgb[0] / 255,
               g = _rgb[1] / 255,
               b = _rgb[2] / 255,
               a = _alpha;

        double max = Math.max(Math.max(r, g), b), min = Math.min(Math.min(r, g), b);
        double h, s, l = (max + min) / 2, d = max - min;

        if (max == min) {
            h = s = 0;
        } else {
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

            if (max == r) {
                h = (g - b) / d + (g < b ? 6 : 0);
            } else if (max == g) {
                h = (b - r) / d + 2;
            } else { // max == b
                h = (r - g) / d + 4;
            }
            h /= 6;
        }
        return new HSL(h * 360, s, l, a);
    }

    public static class HSL {
        public double h, s, l, a;

        public HSL(double hue, double saturation, double lightness, double alpha) {
            this.h = hue;
            this.s = saturation;
            this.l = lightness;
            this.a = alpha;
        }
    }
}
