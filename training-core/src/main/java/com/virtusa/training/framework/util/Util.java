/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.framework.util;

import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import java.lang.reflect.Array;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * A collection of commonly used functions.
 */
public class Util {

    private static final Logger LOG = LoggerFactory.getLogger(Util.class);

    /**
     * Prevents public instantiation.
     */
    private Util() {
    }

    /**
     * The empty string object.
     */
    public static final String EMPTY = "";

    /**
     * Determines if the given object is {@code null} or empty depending on the
     * semantics of the object's type. The following types are supported:
     * <ul>
     * <li>String</li>
     * <li>Collection</li>
     * <li>Map</li>
     * <li>Iterator</li>
     * <li>Enumeration</li>
     * <li>object and primitive array</li>
     * </ul>
     * If the given object is non {@code null}, and is not one of the supported
     * types, {@code false} is returned.
     *
     * @param o The object to test.
     * @return {@code true} if the given object is {@code null}, or semantically
     * empty depending on the object type; {@code false} otherwise.
     */
    public static boolean isEmpty(Object o) {
        if (o == null) {
            return true;
        }
        if (o instanceof String) {
            return StringUtils.isBlank((String) o);
        }
        if (o instanceof Collection<?>) {
            return ((Collection<?>) o).isEmpty();
        }
        if (o instanceof Map<?, ?>) {
            return ((Map<?, ?>) o).isEmpty();
        }
        if (o instanceof Iterator<?>) {
            return !((Iterator<?>) o).hasNext();
        }
        if (o instanceof Enumeration<?>) {
            return !((Enumeration<?>) o).hasMoreElements();
        }
        if (o.getClass().isArray()) {
            return Array.getLength(o) == 0;
        }
        return false;
    }

    /**
     * @param <T> The implied type of the iterator elements.
     * @return An iterator over an empty collection of the implied type.
     */
    public static <T> Iterator<T> emptyIterator() {
        return Collections.<T>emptySet().iterator();
    }

    /**
     * @param <T> The implied type of elements held in the returned collection.
     * @return An empty collection of elements with the implied type.
     */
    public static <T> Collection<T> emptyCollection() {
        return emptySet();
    }

    /**
     * @param <T> The implied type of elements held in the returned set.
     * @return An empty set of elements with the implied type.
     */
    public static <T> Set<T> emptySet() {
        return Collections.emptySet();
    }

    /**
     * @param <T> The implied type of elements held in the returned list.
     * @return An empty list of elements with the implied type.
     */
    public static <T> List<T> emptyList() {
        return Collections.emptyList();
    }

    /**
     * @param <K> The implied key type.
     * @param <V> The implied value type.
     * @return An empty map of entries with implied key, and value types.
     */
    public static <K, V> Map<K, V> emptyMap() {
        return Collections.emptyMap();
    }

    /**
     * A {@code null}-safe way to retrieve a boolean value from a
     * {@link ValueMap}.
     *
     * @param p Contains the boolean property.
     * @param k The property key.
     * @return The boolean value, or {@code false} if not found.
     */
    public static boolean getBoolean(ValueMap p, String k) {
        Boolean b = p.get(k, Boolean.class);
        return b != null && b;
    }

    /**
     * @param r Request of which to get the respective session object.
     * @return The session associated with the given request object, or
     * {@code null} if unable to get session object.
     */
    public static Session sessionOf(SlingHttpServletRequest r) {
        ResourceResolver resolver = r.getResourceResolver();
        if (isEmpty(resolver)) {
            return null;
        }
        return resolver.adaptTo(Session.class);
    }

    private static Map<String, String> codes = null;

    public static String getMappedCode(String shortCode) {

        if (codes == null) {

            codes = new HashMap<String, String>();
            codes.put("sci", "Science");
            codes.put("soc", "Social");
            codes.put("ma", "Math");
            codes.put("mu", "Music");
            codes.put("wl", "Languages");
            codes.put("art", "Art");
            codes.put("rla", "Rla");
        }
        String tCode = shortCode.toLowerCase();
        if (codes.containsKey(tCode)) {
            return codes.get(tCode).toLowerCase();
        }

        return tCode;
    }

    /**
     * Gets the url parts.
     *
     * @param url the url
     * @return the url parts
     */
    public static Map<String, String> getUrlParts(String url) {
        Map<String, String> parts = new HashMap<String, String>();
        URL aURL;
        try {
            if (!StringUtils.startsWithIgnoreCase(url, "http")) {
                url = "http://" + url;
            }
            aURL = new URL(url);
            String protocol = aURL.getProtocol();
            String authority = aURL.getAuthority();
            String host = aURL.getHost();
            int port = aURL.getPort();
            String path = aURL.getPath();
            String query = aURL.getQuery();
            String filename = aURL.getFile();
            String ref = aURL.getRef();
            parts.put(Constants.PROTOCOL, protocol);
            parts.put(Constants.AUTHORITY, authority);
            parts.put(Constants.HOST, host);
            parts.put(Constants.PORT, "" + port);
            parts.put(Constants.PATH, path);
            parts.put(Constants.QUERY, query);
            parts.put(Constants.FILENAME, filename);
            parts.put(Constants.REF, ref);
        } catch (MalformedURLException e) {
            LOG.error("Error in URL", e);
        }

        return parts;
    }

}
