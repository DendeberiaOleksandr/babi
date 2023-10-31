package org.babi.backend.url;

public class UrlUtils {

    public static String encodeUrlIncompatibleCharacters(String url) {
        url = url.replace("/", "%2F");
        url = url.replace("+", "%2B");
        return url;
    }

    public static String decodeUrlIncompatibleCharacters(String url) {
        url = url.replace("%2F", "/");
        url = url.replace("%2B", "+");
        return url;
    }

}
