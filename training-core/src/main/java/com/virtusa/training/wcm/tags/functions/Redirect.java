package com.virtusa.training.wcm.tags.functions;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.foundation.ELEvaluator;
import org.apache.commons.lang.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.apache.commons.lang.StringUtils.isEmpty;

/**
 * Performs a server-side redirect.
 *
 * @author Anant Mittal
 */
public class Redirect extends TagSupport {

    private static final Logger log = LoggerFactory.getLogger(Redirect.class);

    private boolean sendError = false;

    @Override
    public int doEndTag() throws JspException {

        Boolean show = getContextAttribute( "show" );
        Boolean design = getContextAttribute( "design" );

        boolean isEditMode = show == null ? false : show.booleanValue();
        boolean isDesignMode = design == null ? false : design.booleanValue();

        if ( isEditMode || isDesignMode ) return EVAL_PAGE;

        String target = getRedirectTarget();
        if ( isEmpty( target ) && !sendError ) return EVAL_PAGE;

        setupRedirect( target );
        return SKIP_PAGE;
    }

    public void setSendError( boolean b ) {
        sendError = b;
    }

    private void setupRedirect( String target ) throws JspException {
        Page currentPage = getContextAttribute( "currentPage" );
        SlingHttpServletResponse response = getContextAttribute( "slingResponse" );
        //The target must be checked here - if an empty target then you send the error, not just if
        //the target equals the current page (which you check to ensure you don't get any infinite loops.
        if ( StringUtils.isNotEmpty(target) && !target.equals( currentPage.getPath() ) ) {
            response.setStatus( HttpServletResponse.SC_MOVED_PERMANENTLY );
//            response.setHeader( "Location", UtilityFunctions.createValidLink( target, "" ) );
            response.setHeader( "Connection", "close" );
        } else if ( sendError ) {
            try {
                response.sendError( HttpServletResponse.SC_NOT_FOUND );
            } catch ( IOException e ) {
                throw new JspException( e );
            }
        }
    }

    private String getRedirectTarget() {
        String target = getPageProperty( "redirectTarget" );
        SlingHttpServletRequest request = getContextAttribute( "slingRequest" );
        String result = ELEvaluator.evaluate( target, request, pageContext );
        return result == null ? null : result.trim();
    }

    private <T> T getContextAttribute( String key ) {

        @SuppressWarnings( "unchecked" )
        T value = (T) pageContext.getAttribute( key );
        return value;
    }

    private <T> T getPageProperty( String key ) {

        Page currentPage = getContextAttribute( "currentPage" );

        @SuppressWarnings( "unchecked" )
        T value = (T) currentPage.getProperties().get( key );
        return value;
    }
}
