package com.bazaarvoice.jless;

import com.bazaarvoice.jless.exception.ParseException;
import org.apache.commons.io.IOUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

public class UrlLoader implements Loader {

    @Override
    public String load(URI uri) {
        try {
            URL url = uri.toURL();

            InputStream in;
            try {
                in = url.openStream();
            } catch (FileNotFoundException ffne) {
                throw new ParseException("LESS stylesheet file not found: " + uri);
            }

            try {
                ByteArrayOutputStream buf = new ByteArrayOutputStream();
                byte[] bytes = new byte[8192];
                int len;
                while ((len = in.read(bytes)) != -1) {
                    buf.write(bytes, 0, len);
                }
                return buf.toString("UTF-8");

            } finally {
                IOUtils.closeQuietly(in);
            }
        } catch (IOException ioe) {
            throw new ParseException("Exception reading LESS stylesheet " + uri + ": " + ioe.getMessage(), ioe);
        }
    }
}
