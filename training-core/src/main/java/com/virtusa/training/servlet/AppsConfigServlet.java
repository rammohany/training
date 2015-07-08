package com.virtusa.training.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.jcr.AccessDeniedException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.commons.json.JSONArray;
import org.apache.sling.commons.json.JSONException;
import org.apache.sling.commons.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.SymmetricCrypt;
import com.virtusa.training.exception.GenericException;
import com.virtusa.training.model.AppsConfigBean;
import com.virtusa.training.wcm.util.Constants;
import com.virtusa.training.wcm.util.Util;


@SlingServlet(paths = { "/apps/services/AppsConfigServlet.html" }, methods = { "GET",
		"POST" })
@Properties({ @Property(name = "service.pid", value = "com.virtusa.training.servlet.AppsConfigServlet") })
public class AppsConfigServlet extends BaseSlingServlet {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

	/** The Constant LOG. */
	private static final Logger LOG = LoggerFactory
			.getLogger(AppsConfigServlet.class);

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
	 * .sling.api.SlingHttpServletRequest,
	 * org.apache.sling.api.SlingHttpServletResponse)
	 */
	protected void doPost(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {

		String action = request.getParameter(Constants.ACTION);
		response.setHeader("Cache-Control", Constants.NOCACHE_HEADER);
		LOG.debug("request.getHeader() === "
				+ request.getHeader(Constants.REFERER));
		String path = request.getHeader(Constants.REFERER);

		Map<String, String> map = Util.getUrlParts(path);

		String nodePath = map.get(Constants.PATH);

		if (nodePath.contains(Constants.PROPRTY_KEY_SUB_STRING)) {
			nodePath = nodePath.substring(0, nodePath
					.indexOf(Constants.PROPRTY_KEY_SUB_STRING));
		}

		LOG.debug("action === " + action);
		LOG.debug("nodepath === " + nodePath);
		Session session = request.getResourceResolver().adaptTo(Session.class);
		

		if (Constants.VALIDATE.equalsIgnoreCase(action)) {
			String nodeName = request.getParameter(Constants.PROPERTY_NAME);
			LOG.info("nodeName === " + nodeName);
			boolean isDuplicate = this.getAppsConfigService().isDuplicate(
					nodeName, nodePath);
			try {
				response.getWriter().write(Boolean.toString(!isDuplicate));
			} catch (IOException e) {
				LOG.error("IOException in doPost method of AppsConfigServlet",
						e);
			}
		} else if (Constants.GET_ALL.equalsIgnoreCase(action)) {
			try {
				String allNodes = getAllProperties(nodePath, session);
				if (allNodes != null) {
					response.getWriter().write(allNodes);
				}
			} catch (Exception e) {
				LOG.error("Exception in doPost method of AppsConfigServlet", e);
			}

		} else if (Constants.SAVE.equalsIgnoreCase(action)) {
			try {
				String responseStr = saveProperty(request, nodePath);
				response.setStatus(200);
				response.getWriter().write("{success:" + responseStr + "}");
			} catch (RepositoryException e) {
				response.setStatus(500);
				response.getWriter().write("{success:false}");
				LOG.error("Repository Exception of doPost method of save "
						+ "condtion in AppsConfigServlet", e);
			}
		} else if (Constants.PUBLISH_NODES.equalsIgnoreCase(action)) {
			LOG.debug("AppsConfigServlet publishNodes Method check condition");
			String publishList = request.getParameter(Constants.PUBLISH_ID);
			String status = Constants.FALSE;
			boolean publishStatus = false;

			if (publishList != null) {
				try {
					publishStatus = this.getAppsConfigService().publishNodes(
							session, publishList);
					LOG.debug("publishStatus:" + publishStatus);
					if (publishStatus) {
						status = Constants.TRUE;
					}

				} catch (AccessDeniedException e) {

					LOG.error("AccessDeniedException in doPost method "
							+ "of AppsConfigServlet:", e);
					status = "You dont have permission to publish "
							+ "AppsConfigNodes";
				}
				try {
					JSONObject jsonObj = new JSONObject();
					jsonObj.put(Constants.STATUS_PROP, status);
					LOG.debug("jsonObj response" + jsonObj.toString());
					response.setContentType(Constants.APPLICATION_JSON);
					response.getWriter().write(jsonObj.toString());
				} catch (JSONException e) {
					LOG.error(
							"JSONException of doPost method of publish condtion in"
									+ " AppsConfigServlet", e);
				}
			}

		}else if (Constants.PUBLISH_ALL_NODES.equalsIgnoreCase(action)) {
			LOG.debug("AppsConfigServlet publishAllNodes Method check condition"
							+ nodePath);
			String status = Constants.FALSE;
			if (nodePath != null) {
				try {
					nodePath = this.getAppsConfigService()
							.getAppsConfigCollectionPath(session, nodePath);
					LOG.debug("AppsConfigServlet publishAllNodes nodePath"
							+ nodePath);
					boolean publishAllStatus = this.getAppsConfigService()
							.publishAllNodes(session, nodePath);
					LOG.debug("AppsConfigServlet publishAllNodes Methods status:"
									+ publishAllStatus);
					if (publishAllStatus) {
						status = Constants.TRUE;
					}
					try {
						JSONObject jsonObj = new JSONObject();
						jsonObj.put(Constants.STATUS_PROP, status);
						LOG.debug("jsonObj response" + jsonObj.toString());
						response.setContentType(Constants.APPLICATION_JSON);
						response.getWriter().write(jsonObj.toString());
					} catch (JSONException e) {
						LOG.error(
								"JSONException of doPost method of publishAll condtion in"
										+ " AppsConfigServlet", e);
					}
				} catch (Exception e) {
					LOG.error("error in publishAllNodes method", e);
				}
			}
		}
	}

	/**
	 * Save Property.
	 * 
	 * @param request
	 * @return
	 * @throws RepositoryException
	 */
	protected String saveProperty(SlingHttpServletRequest request,
			String nodepath) throws RepositoryException {
		LOG.info("entering saveProperty method of AppsConfigServlet");

		String propertyName = request
				.getParameter(Constants.SLASH_PROPERTY_NAME);
		String authorValue = request.getParameter(Constants.SLASH_AUTHOR_VALUE);
		String publishValue = request
				.getParameter(Constants.SLASH_PUBLISH_VALUE);

		String encrypt = request.getParameter(Constants.SLASH_ENCRYPT);
		LOG.debug("encrypt selected:" + encrypt);
		AppsConfigBean appsConfigBean = new AppsConfigBean();
		appsConfigBean.setPropertyName(propertyName);

		if (encrypt != null
				&& StringUtils.equalsIgnoreCase(Constants.ON, encrypt)) {
			if (!StringUtils.isEmpty(authorValue)
					&& SymmetricCrypt.decrypt(authorValue) == null) {
				appsConfigBean.setAuthorValue(SymmetricCrypt
						.encrypt(authorValue));
			} else {
				appsConfigBean.setAuthorValue(authorValue);
			}
			if (SymmetricCrypt.decrypt(publishValue) == null) {
				appsConfigBean.setPublishValue(SymmetricCrypt
						.encrypt(publishValue));
			} else {
				appsConfigBean.setPublishValue(publishValue);
			}

		} else {
			appsConfigBean.setAuthorValue(authorValue);
			appsConfigBean.setPublishValue(publishValue);
		}

		LOG.info("nodeName============"
				+ request.getParameter(Constants.NODENAME));
		appsConfigBean.setNodeName(request.getParameter(Constants.NODENAME));

		String response;

		try {
			this.getAppsConfigService().saveProperty(appsConfigBean, nodepath);
			response = Constants.TRUE;
		} catch (GenericException e) {
			LOG.error("GenericException of saveProperty method in "
					+ "AppsConfigServlet", e);
			response = Constants.FALSE;
		}
		LOG.info("leaving saveProperty method of AppsConfigServlet "
				+ "with response " + response);
		return response;
	}

	/**
	 * Gets the all properties.
	 * 
	 * @return
	 */
	protected String getAllProperties(String nodepath, Session session) {

		LOG.info("entering getAllNodes method of AppsConfigServlet" + nodepath);
		List<AppsConfigBean> appsconfigList;
		JSONObject jsonObj = new JSONObject();
		JSONArray array = new JSONArray();
		StringWriter writer = new StringWriter();

		try {
			appsconfigList = this.getAppsConfigService().getAllProperties(
					nodepath, session);
		} catch (GenericException e) {
			LOG.error("Error in getAllVanities ", e);
			return null;
		}

		long size = appsconfigList.size();
		LOG.debug("appsconfig list size = " + size);

		if (size != 0) {
			for (int i = 0; i < size; i++) {
				AppsConfigBean appsConfig = appsconfigList.get(i);
				JSONObject curObj = new JSONObject();
				try {
					curObj.put(Constants.QUERRY_PATH, appsConfig.getPath());
					curObj.put(Constants.PROPERTY_NAME_PROP, appsConfig
							.getPropertyName());
					curObj.put(Constants.AUTHOR_VALUE, appsConfig
							.getAuthorValue());
					curObj.put(Constants.PUBLISH_VALUE, appsConfig
							.getPublishValue());
					array.put(curObj);
				} catch (JSONException e) {
					LOG.error("JSONException ", e);
				}
			}
		}
		try {
			jsonObj.put(Constants.RESULT, array);
			jsonObj.write(writer);
		} catch (JSONException je) {
			LOG.error("exception in getAllNodes", je);
		}
		LOG.info("leaving getAllNodes method of AppsConfigServlet");
		return writer.getBuffer().toString();
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.sling.api.servlets.SlingAllMethodsServlet#doPost(org.apache
	 * .sling.api.SlingHttpServletRequest,
	 * org.apache.sling.api.SlingHttpServletResponse)
	 */
	protected void doGet(SlingHttpServletRequest request,
			SlingHttpServletResponse response) throws IOException {
		doPost(request, response);
	}

}