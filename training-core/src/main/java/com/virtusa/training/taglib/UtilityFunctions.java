/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.taglib;


import static com.virtusa.training.framework.util.Util.emptyIterator;
import static com.virtusa.training.framework.util.Util.emptySet;
import static com.virtusa.training.framework.util.Util.isEmpty;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.Random;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ValueMap;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;
import com.day.cq.wcm.api.WCMMode;
import com.day.cq.wcm.commons.WCMUtils;
import com.day.cq.wcm.foundation.ELEvaluator;
//import com.pearson.imt.pearsonsampling.wcm.util.LinkUtilities;
//import com.day.image.Layer;
//import com.day.text.Text;
import com.day.cq.wcm.foundation.Image;

/**
 * Contains a collection of static methods to be used as custom tag functions.
 *
 * @author unascribed
 * @author Scott Flesher
 */
public class UtilityFunctions {

    public static final int SITE_PAGE_DEPTH = 6;
    public static final int LANGUAGE_PAGE_DEPTH = 5;
    public static final String PROP_SHORT_TITLE = "jcr:title";
    public static final String PROP_LONG_TITLE = "pageTitle";
    public static final String PROP_BROWSER_TITLE = "browserTitle";
    public static final String PROP_NAVIGATION_TITLE = "navTitle";
    public static final String PROP_H1_TITLE = "h1Title";
    public static final String COMPONENT_PATH_START = "jcr:content";
    public static final String FORBIDDEN_ID_EXP = "[^a-zA-Z0-9_-]";
    public static final String SITE_DEFAULT_LANGUAGE = "en";
    public static final String USER_GENERATED_PATH = "/content/usergenerated";
    public static final String PROP_LANG_CONFIG_PATH = "langConfigPath";
    public static final String PROP_SITE_CONFIG_NAME = "siteConfigName";
    
    public static final String BROWSER_PAGE_TITLE_TOKEN = "<!--[{[browserpagetitle]}]-->";
    public static final String PAGE_DESCRIPTION_TOKEN = "<!--[{[pagedescription]}]-->";
    public static final String PAGE_KEYWORDS_TOKEN = "<!--[{[pagekeywords]}]-->";

    /**
     * Prevents public instantiation.
     */
    private UtilityFunctions() {}

    /**
     * Indicates the if the given key is hashed to a non-empty value in the
     * given value map.  If so, the key is returned, otherwise {@code null} is
     * returned.  If the provided value map is {@code null}, {@code null} is
     * immediately returned.
     *
     * @param pageProperties The value map from which to check the given key.
     * @param key The key to check in the given value map.
     * @return The given key or {@code null} depending whether the key is found
     * in the given value map.
     */
    private static String getPropertySource(
        final ValueMap pageProperties, final String key ) {

        if ( pageProperties == null ) return null;

        String value = pageProperties.get( key, String.class );
        if ( StringUtils.isEmpty( value ) ) return null;

        return key;
    }

    private static String getProperty(
        final ValueMap pageProperties,
        final String key,
        final String defaultValue ) {

        String source = getPropertySource( pageProperties, key );
        if ( StringUtils.isEmpty( source ) ) {
            return defaultValue;
        }
        return pageProperties.get( source, String.class );
    }

    private static String getPropertySourceFallback(
        final ValueMap pageProperties,
        final String key,
        final String fallbackKey ) {
        
        if ( pageProperties == null ) return null; 

        String value = pageProperties.get( key, String.class );
        if ( StringUtils.isNotEmpty( value ) ) return key;

        value = pageProperties.get( fallbackKey, String.class );
        if ( StringUtils.isNotEmpty( value ) ) return fallbackKey;

        return null;
    }

    private static String getPropertyFallback(
        final ValueMap pageProperties,
        final String key,
        final String fallbackKey,
        final String defaultValue ) {

        String source = getPropertySourceFallback( pageProperties, key, fallbackKey );

        if ( StringUtils.isNotEmpty( source ) ) {
            return pageProperties.get( source, String.class );
        }

        return defaultValue;
    }

    public static String getPageTitle( ValueMap pageProperties, String defaultValue ) {
        return getPropertyFallback( pageProperties, PROP_LONG_TITLE, PROP_SHORT_TITLE, defaultValue );
    }

    public static String getShortTitle( ValueMap pageProperties, String defaultValue ) {
        return getProperty( pageProperties, PROP_SHORT_TITLE, defaultValue );
    }

    public static String getNavigationTitle( ValueMap pageProperties, String defaultValue ) {
        return getPropertyFallback( pageProperties, PROP_NAVIGATION_TITLE, PROP_SHORT_TITLE, defaultValue );
    }

    /**
     * Retrieves the indicated property from the given resource.  If either the
     * given resource or key are {@code null} or empty, {@code null} is
     * returned.  If the given type is empty, it will be defaulted to
     * {@code String.class}.
     * @param resource The resource from which to retrieve the property.
     * @param key The property key.
     * @param typeName The expected type of the indicated property as a fully
     * qualified class name.
     * @return The indicated property from the given resource, or {@code null}
     * if not available.
     * @throws ClassNotFoundException if the indicated type is not a valid
     * class.
     */
    public static Object getProperty(
        Resource resource, String key, String typeName )
        throws ClassNotFoundException {

        if ( isEmpty( resource ) || isEmpty( key ) ) return null;

        Class<?> type;
        if ( isEmpty( typeName ) ) {
            type = String.class;
        } else {
            type = Class.forName( typeName );
        }

        ValueMap properties = resource.adaptTo( ValueMap.class );
        return properties.get( key, type );
    }

    public static String getBrowserTitle( ValueMap pageProperties, String defaultValue ) {
        if ( pageProperties == null ) return defaultValue;
        String browserTitle = pageProperties.get( PROP_BROWSER_TITLE, String.class );
        return
            StringUtils.isNotEmpty( browserTitle ) ?
            browserTitle :
            getPageTitle( pageProperties, defaultValue );
    }

    public static String getH1Title( ValueMap pageProperties, String defaultValue ) {
        if ( pageProperties == null ) return defaultValue;
        String h1Title = pageProperties.get( PROP_H1_TITLE, String.class );
        return
            StringUtils.isNotEmpty( h1Title ) ?
            pageProperties.get(PROP_H1_TITLE, String.class) :
            getPageTitle(pageProperties, defaultValue);
    }

    public static String getComponentRelativePath( String path ) {
        path = StringUtils.trimToEmpty( path );
        if ( path.endsWith( COMPONENT_PATH_START ) ) return "";
        if ( path.indexOf( COMPONENT_PATH_START ) > 0 ) {
            int index = path.indexOf( COMPONENT_PATH_START );
            return path.substring( index + COMPONENT_PATH_START.length() + 1 );
        }
        return path;
    }

    public static String parseDocumentSafeId( String str, String delim ) {
        delim = delim.replaceAll( FORBIDDEN_ID_EXP, "" );
        str = str.replaceAll( FORBIDDEN_ID_EXP, delim );

        while( str.startsWith( delim ) ) {
            str = str.replaceFirst( delim,"" );
        }

        /* XML/HTML DTDs require a letter as the first character for the id attribute*/
        if( !Character.toString( str.charAt( 0 ) ).matches( "[a-zA-Z]" ) ) {
            str = "a" + str;
        }

        return str.replaceAll( FORBIDDEN_ID_EXP, delim );
    }

    public static String getComponentDivId( Resource resource, String delim ) {
        String id = null;

        if ( resource != null ) {
            delim = delim.replaceAll( FORBIDDEN_ID_EXP, "" );
            id = getComponentRelativePath( resource.getPath() );
            id = parseDocumentSafeId( id, delim );
        }

        return id;
    }

    public static String getComponentDivId( Resource resource ) {
        return getComponentDivId( resource, "_" );
    }



    /**
     * Wrapper for {@link StringEscapeUtils#escapeHtml(String)}.
     *
     * @param html The HTML string to escape.
     * @return the result of the wrapped method call.
     */
    public static String escapeHtml( String html ) {
        return StringEscapeUtils.escapeHtml( html );
    }

    /**
     * @param p The page from which to retrieve the indicated page property.
     * @param key The key used to look up the value to return.
     * @return The value mapped to the given key in the provided map as a
     * string.
     */
    public static String escapeProperty( Page p, String key ) {
        Object value = p.getProperties().get( key );
        return escapeHtml( value == null ? "" : value.toString() );
    }

    /**
     * @param p The page from which to retrieve keywords.
     * @return A comma separated list of keywords from the given page node, and
     * the site config page (the current page’s level SITE_PAGE_DEPTH ancestor). 
     */
    public static String getKeywords( Page p ) throws RepositoryException {
        Set<String> tags = new HashSet<String>();
        //tags.addAll( getPageKeywords( p ) );
       // tags.addAll( getSiteConfigKeywords( p ) );

        StringBuilder tagList = new StringBuilder();
        for ( String tag : tags ) {
            if ( tagList.length() > 0 ) tagList.append( ", " );
            tagList.append( tag );
        }

        return escapeHtml( tagList.toString() );
    }


    private static Set<String> getPageKeywords( Page p ) {
    	Set<String> tags = new HashSet<String>();
        String list = WCMUtils.getKeywords( p, false );
        if ( isEmpty( list ) ) return emptySet();

        String[] tokens = list.split( ",\\s*" );
        Collections.addAll( tags, tokens );

        return tags;
    }

    public static Set<String> getSiteConfigKeywords(Page p ) throws RepositoryException {
        p = getSiteRoot( p );
        return getPageKeywords( p );
    } 

    /**
     * Provides the title for the given page by looking at the following
     * locations (returns the first found):
     * <ul>
     *   <li>{@code getTitle} property of given page</li>
     *   <li>{@code jcr:title} property of given page</li>
     *   <li>provided default</li>
     * </ul>
     *
     * @param page The page to get the title for.
     * @param d3fault The default title, if the given page does not provides its
     * own title.
     * @return The title of the given page for use in the browser &lt;title&gt;
     * tag.
     */
    public static String getTitle( Page page, String d3fault ) {
        String title = page.getTitle();
        if ( !StringUtils.isEmpty( title ) ) return title;
        Object value = page.getProperties().get( PROP_SHORT_TITLE );
        title = value == null ? "" : value.toString();
        if ( !StringUtils.isEmpty( title ) ) return title;
        return d3fault;
    }

    /**
     * Provides the title for the given page by looking at the following
     * locations (returns the first found):
     * <ul>
     *   <li>{@code browserTitle} property of given page</li>
     *   <li>{@code pageTitle} property of given page</li>
     *   <li>{@code jcr:title} property of given page</li>
     *   <li>provided default</li>
     * </ul>
     *
     * @param page The page to get the title for.
     * @param defaultTitle The default title, if the given page does not
     * provide its own title.
     * @return The title of the given page for use in the browser &lt;title&gt;
     * tag.
     */
    public static String getPageBrowserTitle(Page page, String defaultTitle) {
         return escapeHtml( getBrowserTitle( page.getProperties(), defaultTitle ) );
    }
    
    /**
     * Provides the description for the given page by getting the jcr:description property.
     *
     * @param page The page to get the title for.
     * @return The description of the given page for use in the HTML head
     */
    public static String getPageDescription(Page page) {
        final String retVal;
        if (page != null) {
            retVal = escapeHtml( getProperty(page.getProperties(), "jcr:description", "") );
        } else {
            retVal = "";
        }
        return retVal;
    }



    /**
     * @param p The page from which to get the configured redirect URL.
     * @return The redirect URL configured for the given page.  If not
     * configured, {@code null} is returned.
     */
    public static String getRedirectUrl( Page p ) {
        return p.getProperties().get( "redirectTarget", String.class );
    }

    /**
     * @param p The page in question.
     * @return {@code true} if the given page has been configured with a
     * redirect URL; {@code false} otherwise.
     */
    public static boolean isRedirect( Page p ) {
        return !StringUtils.isEmpty( getRedirectUrl( p ) );
    }

    /**
     * @param p The page in question.
     * @return {@code true} if the header should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hideHeader" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showHeader( Page p ) {
        Boolean hide = p.getProperties().get( "hideHeader", Boolean.class );
        return hide == null || !hide;
    }

    /**
     * @param p The page in question.
     * @return {@code true} if the footer should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hideFooter" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showFooter( Page p ) {
        Boolean hide = p.getProperties().get( "hideFooter", Boolean.class );
        return hide == null || !hide;
    }

    /**
     * @param p The page in question.
     * @return {@code true} if the Right Rail should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hideRightRail" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showRightRail( Page p ) {
        Boolean hide = p.getProperties().get( "hideRightRail", Boolean.class );
        return hide == null || !hide;
    }

    /**
     * @param p The page in question.
     * @return {@code true} if the Left Nav should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hideLeftNavigation" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showLeftNavigation( Page p ) {
        Boolean hide = p.getProperties().get( "hideLeftNavigation", Boolean.class );
        return hide == null || !hide;
    }
    /**
     * @param p The page in question.
     * @return {@code true} if the BreadCrumb should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hideBreadCrumb" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showBreadCrumb( Page p ) {
        Boolean hide = p.getProperties().get( "hideBreadCrumb", Boolean.class );
        return hide == null || !hide;
    }


    /**
     * @param p The page in question.
     * @return {@code true} if the Page Title Component should be shown for the given page;
     * {@code false} otherwise.  Inspects the "hidePageTitle" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showPageTitle( Page p ) {
        Boolean hide = p.getProperties().get( "hidePageTitle", Boolean.class );
        return hide == null || !hide;
    }

        /**
     * @param p The page in question.
     * @return {@code true} if the border should display around a parsys;
     * {@code false} otherwise.  Inspects the "showContainerBorder" property of the given
     * page.  If the property is not set, {@code true} is returned.
     */
    public static boolean showContainerBorder( Page p ) {
    	if( p==null) return false;
        Boolean show = p.getProperties().get( "showContainerBorder", Boolean.class );
        
        if( show== null)
        	return false;
        return show;
    }
    
    /**
     * Indicates whether the given page is a site root, or "level SITE_PAGE_DEPTH" page.
     * Accounts for the fact the getDepth treats /content is level 1, but 
     * getAbsoluteParent treats it as 0.
     *
     * @param p The page in question.
     * @return {@code true} if the given page is a site root, or "level SITE_PAGE_DEPTH" page;
     * {@code false} otherwise.
     */
    public static boolean isSiteRoot( Page p ) {
        return ( p.getDepth() == SITE_PAGE_DEPTH +1 );
    }

    /**
     * Identifies the site root, or "level SITE_PAGE_DEPTH" page, of the given page ( /content/ped/penak12/us/pearsonsampling/[language]/[site]
     *
     * @param p The page in question.
     * @return The site root for the given page.
     * 
     * @throws RepositoryException
     */
    public static Page getSiteRoot( Page p ) throws RepositoryException {
    	Page siteRoot = p.getAbsoluteParent( SITE_PAGE_DEPTH );
        if(siteRoot == null) {
        	//The current page is not under the ped/penak12/us/[language]/[site]/.. structure
        	Resource currentRes = p.adaptTo(Resource.class);
        	ResourceResolver resolver = currentRes.getResourceResolver();
        	PageManager pageManager = resolver.adaptTo( PageManager.class );
        	
        	String resPath = currentRes.getPath();
        	//If the current page is under /content/usergenerated, adjust the language config level
        	if(resPath.startsWith(USER_GENERATED_PATH)) {
        		//Trim the first two levels off the path
        		resPath = resPath.replace(USER_GENERATED_PATH, "");
        	}
        	//Get the path of the level SITE_PAGE_DEPTH parent
        	int pos = StringUtils.ordinalIndexOf(resPath, "/", SITE_PAGE_DEPTH + 1);
        	resPath = resPath.substring(0, pos);
        	
        	//Set the site root to the site page referenced in the language folder
        	Resource languageFolder = resolver.getResource(resPath);
        	Node languageNode = languageFolder.adaptTo(Node.class);
        	if(languageNode.hasProperty(PROP_SITE_CONFIG_NAME) && languageNode.hasProperty(PROP_LANG_CONFIG_PATH)) {
    			siteRoot = pageManager.getPage(languageNode.getProperty(PROP_LANG_CONFIG_PATH).getString() + "/" + languageNode.getProperty(PROP_SITE_CONFIG_NAME).getString());
    		} 	
        } else {
        	String siteRootPath = siteRoot.getPath();
	    	if(siteRootPath.startsWith(USER_GENERATED_PATH)) {
	    		siteRoot = p.getAbsoluteParent(5);
	    		siteRootPath = siteRoot.getPath();
	    		// Trim the first two levels off the path
	    		siteRootPath = siteRootPath.replace(USER_GENERATED_PATH, "");
	    		siteRoot = p.getPageManager().getPage(siteRootPath);
	    	}
        }
        return siteRoot;
    }

    /**
     * Identifies the language config, or "level LANGUAGE_PAGE_DEPTH" page, of the given page.
     *
     * @param p The page in question.
     * @return The site root for the given page.
     */
    public static Page getLanguageConfig( Page p ) {
        Page languageConfig = p.getAbsoluteParent(LANGUAGE_PAGE_DEPTH );
        if(languageConfig == null) {
        	//The current page is not under the ped/penak12/us/pearsonsampling/[language]/[site]/.. structure
        	Resource currentRes = p.adaptTo(Resource.class);
        	ResourceResolver resolver = currentRes.getResourceResolver();
        	PageManager pageManager = resolver.adaptTo( PageManager.class );
        	
        	String resPath = currentRes.getPath();
        	//If the current page is under /content/usergenerated, adjust the language config level
        	if(resPath.startsWith(USER_GENERATED_PATH)) {
        		//Trim the first two levels off the path
        		resPath = resPath.replace(USER_GENERATED_PATH, "");
        	}
        	//Get the path of the level LANGUAGE_PAGE_DEPTH parent
        	int pos = StringUtils.ordinalIndexOf(resPath, "/", SITE_PAGE_DEPTH +1);
        	resPath = resPath.substring(0, pos);
        	
        	//Set the language config to the language page referenced in the language folder
        	Resource languageFolder = resolver.getResource(resPath);
        	Node languageNode = languageFolder.adaptTo(Node.class);
        	try {
        		if(languageNode.hasProperty(PROP_LANG_CONFIG_PATH)) {
        			languageConfig = pageManager.getPage(languageNode.getProperty(PROP_LANG_CONFIG_PATH).getString());
        		}     		
        	} catch (RepositoryException e) {
        		throw new RuntimeException( e );
        	}  	
        }
        return languageConfig;
    }
    
    /**
     * @param page The page from which to retrieve the content.
     * @param path The path under the given page's content resource from which
     * to retrieve the content.  Should not be prefixed with slash ( "/" ).
     * @return The content resource of the given page under the given path.
     */
    public static Resource getResource( Page page, String path ) {

        if ( page == null ) return null;
        if ( StringUtils.isEmpty( path ) ) return page.getContentResource();

        Resource content = page.getContentResource();
        return content.getChild( path );
    }

    /**
     * @param resource The returned node will be under the given path,
     * relative to this resource.
     * @param path The relative path under the provided resource where to
     * retrieve the node.
     * @return The node under the given path relative to the path of the
     * provided resource. If the provided resource is {@code null}, {@code null}
     * is returned.  If the given path is empty, the provided resource is
     * returned.
     */
    public static Resource getResource( Resource resource, String path ) {
        if ( resource == null ) return null;
        if ( StringUtils.isEmpty( path ) ) return resource;
        return resource.getChild( path );
    }

    /**
     * @param request The current request. If {@code null}, {@code null} is
     * returned.
     * @param absolutePath The absolute path of a resource to return. If
     * {@code null} or empty, {@code null} is returned.
     * @return The resource resolved from the given absolute path.
     */
    public static Resource getResource(
        SlingHttpServletRequest request,
        String absolutePath ) {

        if ( request == null || StringUtils.isEmpty( absolutePath ) ) {
            return null;
        }
        ResourceResolver resolver = request.getResourceResolver();
        return resolver.getResource( absolutePath );
    }

    /**
     * @param r From which to get the children.
     * @return An iterator over the child nodes of the given resource. If the
     * given resource is {@code null}, an empty iterator is returned.
     */
    public static Iterator<Resource> getChildren( Resource r ) {
        if ( r == null ) return emptyIterator();
        return r.listChildren();
    }

    /**
     * @param r The current request.
     * @param p The page for which to get the site root.
     * @return The site root for the given page as a string.
     */
    public static String getSiteRootPath( SlingHttpServletRequest r, Page p ) throws RepositoryException {
        p = getSiteRoot( p );
        return getResourcePath( r, p );
    }

    public static  SlingHttpServletResponse getBasePageRedirectResponse(
        SlingHttpServletRequest request,
        Page currentPage,
        PageContext pageContext ) {

    	SlingHttpServletResponse response = (SlingHttpServletResponse) pageContext.getResponse();
        WCMMode mode = WCMMode.fromRequest(request);
		boolean show = mode == WCMMode.EDIT;
		boolean design = mode == WCMMode.DESIGN;

		if ( !show && !design ) {
		    // read the redirect target from the 'page properties' and perform the redirect
		    String location = currentPage.getProperties().get( "redirectTarget", String.class );
		    // resolve variables in path
		    location = ELEvaluator.evaluate( location, request, pageContext );
		    if ( !StringUtils.isEmpty( location ) ) {
		        // check for recursion
		        if ( !location.equals( currentPage.getPath() ) ) {
		        	response.setStatus( HttpServletResponse.SC_MOVED_PERMANENTLY );
		        	response.setHeader( "Location",location);
		        	response.setHeader( "Connection", "close" );
		        }
		    }
		}
		return response;
	}

    /**
     * Creates a valid href or link or URL from typical CQ link storage formats.
     * a simple function that converts an authored entered link to a valid link
     * for href use or in a redirect.
     * The function proxies to LinkHelper.createValidHref.
     *
     * @param link - the link to be checked for validity
     * @param defaultLink -
     */
  /*  public static String createValidLink( String link, String defaultLink ) {
    	log.debug( "[createValidLink] {}", link );
        String href = defaultLink;
        if ( StringUtils.isNotEmpty( link ) ) {
            href = LinkUtilities.createValidLink(link);
        }
        log.debug( "[createValidLink] Returning {}", href );
        return href;
    } */

 /*   public static String escapeDayUrl( String url, boolean standardEscape ) {
        if ( StringUtils.isEmpty( url ) ) return "";
        String result = url;
        result = result.replace( "jcr:content", ( "_jcr_content" ) );
        if ( standardEscape ) result = Text.escape( url, '%', true );
        return result;
    }*/

    /**
     * @param request The current request
     * @param page The page of which to get the resource path.
     * @return Using the resource resolver from the given request, will return
     * teh resource path for the given page.
     */
    private synchronized static String getResourcePath(
        SlingHttpServletRequest request,
        Page page ) {

        String pagePath = page.getPath();
        ResourceResolver resolver = request.getResourceResolver();

        return resolver.map( pagePath );
    }

    /**
     * @param uri The URI to CRX resource.
     * @return The "base" form of the indicated resource.  The extension and any
     * selectors will be removed. Example: "/path/to/resource.printable.html"
     * would return as "/path/to/resource"
     */
    public static String getComponentBase( String uri ) {
        int lastSlash = uri.lastIndexOf( "/" );
        String resource = uri.substring( lastSlash + 1 );
        StringTokenizer tokenizer = new StringTokenizer( resource, "." );
        return uri.substring( 0, lastSlash + 1 ) + tokenizer.nextToken();
    }

    /**
     * @param r The current request.
     * @param p The page from which to get the canonical URL.
     * @return The canonical URL of the given page if set. Otherwise,
     * {@code null} is returned.
     */
    public static String getCanonicalUrl( SlingHttpServletRequest r, Page p ) {
        return "http://www.pearson.com" + getResourcePath( r, p ) + ".html";
    }

    /**
     * Indicates whether currently in author mode or not.
     * @param context The current page context.
     * @return {@code true} if in author mode; {@code false} otherwise.
     */
    public static boolean isAuthorMode( PageContext context ) {
        Boolean isEditMode = (Boolean) context.getAttribute( "show" );
        if (isEditMode == null) {
        	isEditMode = new Boolean(false);
        }
        Boolean isDesignMode = (Boolean) context.getAttribute( "design" );
        if (isDesignMode == null) {
        	isDesignMode = new Boolean(false);
        }
        return isEditMode || isDesignMode;
    }


    /**
     * @param resource To adapt to a value map.
     * @return The value map representation of the given resource.
     */
    public static ValueMap adaptToValueMap( Resource resource ) {
        if ( resource == null ) return null;
        return resource.adaptTo( ValueMap.class );
    }

    /**
     * Indicates if the given image matches the provided dimensions.
     *
     * @param image The image in question.
     * @param width The expected width of the given image.
     * @param height The expected height of the given image.
     * @return {@code true} if the size of the given image match the expected
     * provided dimensions; {@code false} otherwise.
     */
 /*   public static boolean isSize( Image image, int width, int height ) {

        if ( image == null ) return false;

        Layer layer;
        try {
            layer = image.getLayer( true, true, true );
            if ( layer == null ) return false;
        } catch ( IOException e ) {
            throw new RuntimeException( e );
        } catch ( RepositoryException e ) {
            throw new RuntimeException( e );
        }

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        return ( layerWidth == width ) && ( layerHeight == height );
    }*/

    /**
     * Returns the given link ends with a ".html" extension.  If the given link
     * already has a ".html" extension, it is returned unchanged.
     *
     * @param link The path to a page resource.
     * @return Ensures the given link has a ".html" extension.
     */
    public static String withHypertextExtension( String link ) {
        if ( StringUtils.isEmpty( link ) ) return "#";
        if ( link.endsWith( ".html" ) ) return link;
        if ( link.startsWith( "/content" ) ) return link + ".html";
        return link;
    }

    /**
     * Get the language for a given page. Defaults to 'en'.
     * @param currentPage Page to retrieve the language from.
     * @return String of the language, defaulting to 'en'.
     */
    public static String getPageLanguage( Page currentPage ) {
        return getPageLanguage( currentPage, SITE_DEFAULT_LANGUAGE );
    }

    /**
     * Get the language for a given page. Default to the passed in value.
     * @param currentPage Page to retrieve the language from.
     * @param defaultLanguage Default value if the language is not set on the
     * page.
     * @return String of the language, defaulting to defaultLanguage.
     */
    public static String getPageLanguage(
        Page currentPage, String defaultLanguage ) {

        if ( currentPage == null ) return defaultLanguage;

        Locale locale = currentPage.getLanguage( false );
        if ( locale == null ) return defaultLanguage;

        String language = locale.getLanguage();
        if ( !StringUtils.isEmpty( language ) ) return language;

        return defaultLanguage;
    }

    
    public static String getPageProperty( Page page, String key ) {
        ValueMap properties = page.adaptTo( ValueMap.class );
        return properties.get( key, String.class );
    }

	public static int getNextRandomNumber() {
		return new Random().nextInt();
	}
    
	/**
	 * Draw.
	 *
	 * @param image the image
	 * @param w the w
	 * @throws IOException Signals that an I/O exception has occurred.
	 */
	public static void draw(Image image, Writer w) throws IOException {
		image.draw(w);
	}
}