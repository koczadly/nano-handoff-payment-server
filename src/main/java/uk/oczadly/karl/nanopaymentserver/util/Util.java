package uk.oczadly.karl.nanopaymentserver.util;

import uk.oczadly.karl.jnano.model.NanoAmount;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author Karl Oczadly
 */
public class Util {
    
    public static URL parseUrl(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
}
