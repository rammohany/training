package com.virtusa.training.servlet;

import java.util.Locale;

import javax.jcr.Session;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.wcm.api.LanguageManager;
import com.virtusa.training.service.AppsConfigService;

/**
 * 
 * The BaseSlingServlet.
 * 
 */
@Component
public abstract class BaseSlingServlet extends SlingAllMethodsServlet {
	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory
			.getLogger(BaseSlingServlet.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 3117462846659846391L;
	
	@Reference(referenceInterface=AppsConfigService.class, bind="setAppsConfigService", unbind="unSetAppsConfigService", cardinality=ReferenceCardinality.MANDATORY_UNARY)
	private AppsConfigService service;


	@Reference(referenceInterface=LanguageManager.class, bind="setLanguageManager", unbind="unSetLanguageManager", cardinality=ReferenceCardinality.MANDATORY_UNARY)
	private LanguageManager languageManager;

	/**
	 * @param service
	 *            LanguageManager
	 */
	protected final void setLanguageManager(
			final LanguageManager languageManager) {
		this.languageManager = languageManager;
	}

	/**
	 * Returns the language manager service.
	 * 
	 * @return LanguageManager
	 */
	protected final LanguageManager getLanguageManager() {
		return this.languageManager;
	}

	/**
	 * @param service
	 *            AppsConfigService
	 */
	protected final void unSetLanguageManager(
			final LanguageManager languageManager) {
		if (this.languageManager == languageManager) {
			this.languageManager = null;
		}
	}

	/**
	 * @param service
	 *            AppsConfigService
	 */
	protected final void setAppsConfigService(final AppsConfigService service) {
		this.service = service;
	}

	/**
	 * @param service
	 *            AppsConfigService
	 */
	protected final void unSetAppsConfigService(final AppsConfigService service) {
		if (this.service == service) {
			this.service = null;
		}
	}

	/**
	 * Returns the AppsConfigService object.
	 * 
	 * @return AppsConfigService
	 */
	protected AppsConfigService getAppsConfigService() {
		return this.service;
	}

	/**
	 * Gets the session.
	 * 
	 * @param request
	 *            the request
	 * @return the session
	 */
	protected Session getSession(SlingHttpServletRequest request) {
		Session session = request.getResourceResolver().adaptTo(Session.class);
		return session;
	}

	/**
	 * Gets the locale.
	 * 
	 * @param request
	 *            the request
	 * @return the locale
	 */
	public Locale getLocale(SlingHttpServletRequest request) {
		LOG.debug("in getLocale method");
		Locale locale = getPageLocale(request);
		;
		if (locale != null) {
			return locale;
		}
		return request.getLocale();

	}

	/**
	 * Gets the page locale.
	 * 
	 * @param resource
	 *            the resource
	 * @return the page locale
	 */
	public Locale getPageLocale(SlingHttpServletRequest request) {
		LOG.debug("in getPageLocale method");
		try {
			Resource resource = request.getResource();
			LOG.debug("returning locale from getPageLocale");
			return getLanguageManager().getLanguage(resource);

		} catch (Exception e) {
			LOG.error("error in getPageLocale", e);
			return null;
		}
	}

	
}
