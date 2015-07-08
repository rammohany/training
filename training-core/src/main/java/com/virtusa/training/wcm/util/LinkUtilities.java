package com.virtusa.training.wcm.util;


//import com.day.text.Text;
//import com.pearson.imt.pearsonsampling.wcm.tags.functions.UtilityFunctions;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility functions related to HTML links.
 */
public class LinkUtilities {

    private static final Logger log = LoggerFactory.getLogger(LinkUtilities.class);

    private static final String POUND_CHARACTER = "#";
    private static final String SLASH_CHARACTER = "/";
    private static final String PERIOD_CHARACTER = ".";
    private static final String COLON_CHARACTER = ":";
    private static final String QUESTION_MARK_CHARACTER = "?";
    private static final String DEFAULT_EXTENSION = "html";
    private static final String DEFAULT_PROTOCOL = "http://";

    private static final String INTERNAL_LINK_START = "/content";

    private LinkUtilities() {
        //prevent instantiation
    }

    /**
     * Utility to build a link from it's parts.
     *
     * If the link is an external link (does not start with /content) the method simply ensures it has a protocol and
     * returns it.
     *
     * For internal links the method each of the parameters (if present) to the link (selector, extension, queryString,
     * link suffix). If an extension is not provided .html is added (the method assumes that for internal links the
     * link being passed in does not already have an extension).
     *
     * If the link in question includes a anchor ID the method preserves the anchor ID.
     *
     * @param link base path
     * @param selector selector string to be added
     * @param extension extension to be added
     * @param queryString query string to be added.
     * @param linkSuffix Link Suffix to be added
     * @return Valid Link
     */
   /* public static String buildValidLink(String link, String selector, String extension,
                                        String queryString, String linkSuffix) {
		if (log.isDebugEnabled()) {
			log.debug("[buildValidLink] link: " + link + " selector: " + selector + " extension: " + extension
					+ "query string: " + queryString + " linkSuffix: " + linkSuffix);
		}
		StringBuilder href = new StringBuilder();
		if (StringUtils.isNotEmpty(link) && isExternalLink(link)) {
            href.append(createValidLink(link));
        } else if (StringUtils.isNotEmpty(link)) {

			String linkAnchor = getLinkAnchor(link);
            String noAnchorLink = stripLinkAnchor(link);

			href.append(UtilityFunctions.escapeDayUrl(noAnchorLink, false));

			if (StringUtils.isNotEmpty(selector)) {
				href.append(".").append(
						Text.escape(selector.trim(), '%', false));
			}

            if (StringUtils.isEmpty(extension)) {
                href.append(".").append(DEFAULT_EXTENSION);
            } else {
                href.append(".").append(extension);
            }

			if (StringUtils.isNotEmpty(linkSuffix)) {
                if (linkSuffix.indexOf(SLASH_CHARACTER) < 0) {
                    href.append(SLASH_CHARACTER);
                }
				href.append(Text.escape(linkSuffix, '%', true));
			}

			if (StringUtils.isNotEmpty(queryString)) {
				if (queryString.indexOf(QUESTION_MARK_CHARACTER) < 0) {
                    href.append(QUESTION_MARK_CHARACTER);
                }
                href.append(queryString);
			}

			if (StringUtils.isNotEmpty(linkAnchor)) {
				href.append(linkAnchor);
			}

		}

		if (log.isDebugEnabled()) {
			log.debug("[buildValidLink] Returning link: {}", href.toString());
		}

		return href.toString();


    }*/

    /**
     * If the link has an anchor on it (includes a # character) this method gets and returns the value after the #
     * character.
     *
     * @param link Link to obtain the anchor tag from.
     * @return String
     */
    public static String getLinkAnchor(String link) {
        String linkAnchor;

        if (StringUtils.isNotEmpty(link) && link.indexOf(POUND_CHARACTER) > 0) {
            linkAnchor = link.substring(link.indexOf(POUND_CHARACTER));
        } else {
            linkAnchor = "";
        }

        return linkAnchor;
    }

    /**
     * If the link has an anchor on it (includes a # character) this method removes it and returns the link without
     * the anchor
     *
     * @param link Link to remove the anchor from
     * @return String
     */
    public static String stripLinkAnchor(String link) {
        String noAnchorLink;

        if (StringUtils.isNotEmpty(link) && link.indexOf(POUND_CHARACTER) > 0) {
            noAnchorLink = link.substring(0, link.indexOf(POUND_CHARACTER));
        } else {
            noAnchorLink = link;
        }

        return noAnchorLink;
    }

    /**
     * Null safe utility for determining if a link is external or not. Currently based solely on wheter the link
     * starts with a /content or not.
     *
     * @param link Link to check
     * @return boolean
     */
    public static boolean isExternalLink(String link) {
        final boolean external;

        if (StringUtils.isNotEmpty(link) && link.startsWith(INTERNAL_LINK_START)) {
            external = false;
        } else {
            external = true;
        }

        return external;
    }

    /**
     * Null safe utility to take a authored link property and make it a valid HREF.
     *
     * Adds .html extension to internal links without an extension.
     *
     * Adds http:// to external links without a protocol
     *
     * @param link String link to converted to href
     * @return String with a valid href or a empty string if a null or empty
     */
   /* public static String createValidLink(String link) {
        final String alteredLink;

        if (StringUtils.isNotEmpty(link)) {
            if (!isExternalLink(link) && link.indexOf(PERIOD_CHARACTER) < 1) {
                //Internal link and needs an extension

                alteredLink = UtilityFunctions.escapeDayUrl(link, false) + PERIOD_CHARACTER + DEFAULT_EXTENSION;
            } else if (isExternalLink(link) && !link.startsWith(SLASH_CHARACTER)&& link.indexOf(COLON_CHARACTER) < 1) {
                //external link without protocol add protocol
                alteredLink = DEFAULT_PROTOCOL + link;
            } else {
                alteredLink = link;
            }
        } else {
            alteredLink = "";
        }

        return alteredLink;
    }*/



}
