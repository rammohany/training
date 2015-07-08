package com.virtusa.training.service.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jcr.AccessDeniedException;
import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.query.Query;
import javax.jcr.query.QueryManager;
import javax.jcr.query.QueryResult;

import org.apache.commons.lang.StringUtils;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.ReferenceCardinality;
import org.apache.felix.scr.annotations.Service;
import org.apache.sling.commons.scheduler.Scheduler;
import org.apache.sling.jcr.api.SlingRepository;
import org.osgi.service.component.ComponentContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.day.cq.commons.jcr.JcrUtil;
import com.day.cq.replication.ReplicationActionType;
import com.day.cq.replication.ReplicationException;
import com.day.cq.replication.Replicator;
import com.virtusa.training.exception.GenericException;
import com.virtusa.training.model.AppsConfigBean;
import com.virtusa.training.service.AppsConfigService;
import com.virtusa.training.wcm.util.Constants;

@Component(label = "Apps Config Service", description = "Provides the Application config variables", immediate = true, metatype = true)
@Properties({
		@Property(name = "service.pid", value = "com.virtusa.training.service.AppsConfigService", propertyPrivate = false),
		@Property(name = "service.description", value = "Provides the Application config variables", propertyPrivate = false)})
@Service({AppsConfigService.class})
public class AppsConfigServiceImpl implements AppsConfigService {
    /**
     * logger object for handling log messages.
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(AppsConfigServiceImpl.class);
    /**
     * period.
     */
    @Property(name="period", value="60")
    private transient String period;
    /**
     * Used in caching the properties.
     */
    private Hashtable<String, String> appsConfigProperties = 
        new Hashtable<String, String>();
    @Reference(referenceInterface=Scheduler.class, bind="setScheduler", unbind="unSetScheduler", cardinality=ReferenceCardinality.MANDATORY_UNARY)
    private Scheduler scheduler;

    
    @Reference(referenceInterface=SlingRepository.class, bind="bindRepository", unbind="unbindRepository", cardinality=ReferenceCardinality.MANDATORY_UNARY)
    protected SlingRepository repository;
    
    @Reference(referenceInterface=Replicator.class, bind="bindReplicator", unbind="unbindReplicator", cardinality=ReferenceCardinality.MANDATORY_UNARY)
    private Replicator replicator;

    protected final void bindReplicator(Replicator replicator) {
        this.replicator = replicator;
    }

    protected final void unbindReplicator(Replicator replicator) {
        if (this.replicator == replicator) {
            this.replicator = null;
        }
    }

    /**
     * Gets the replicator.
     * 
     * @return the replicator
     */
    protected final Replicator getReplicator() {
        LOG.debug("getReplicator==" + this.replicator);
        return this.replicator;
    }

    /**
     * Substitues the locale into the key and get the value. Gets the property.
     * 
     * @param locale
     *            the locale
     * @param key
     *            the key
     * @return the value property
     */
    public String getProperty(Locale locale, String key) {
        return getProperty(locale, key, null);
    }

    /**
     * Substitues the locale into the key and get the value. Gets the property.
     * 
     * @param locale
     *            the locale
     * @param key
     *            the key
     * @param mockValues
     *            mock value map
     * @return the property
     */
    public String getProperty(Locale locale, String key,
            Map<String, String> mockValues) {
        if (mockValues != null) {
            if (mockValues.containsKey(key)) {
                return mockValues.get(key);
            }
        }
        String language;
        if (locale != null) {
            language = locale.getLanguage();
        } else {
            language = Constants.DEFAULT_APPS_CONFIG_LANGUAGE;
        }

        if (locale != null && StringUtils.isEmpty(language)) {
            language = Constants.DEFAULT_APPS_CONFIG_LANGUAGE;
        }
        language = language.toLowerCase();
        String delimiter = Constants.DOT;
        String[] tempKey = key.split(delimiter, 3);
        if (tempKey.length == 2) {
            String propKey;
            propKey = StringUtils.replaceOnce(key,
                    Constants.PROPRTY_KEY_SUB_STRING,
                    Constants.PROPRTY_KEY_SUB_STRING + language
                            + Constants.PROPRTY_KEY_SUB_STRING);
            String value = getProperty(propKey);
            if (StringUtils.isEmpty(value)
                    && !StringUtils.equalsIgnoreCase(language,
                            Constants.DEFAULT_APPS_CONFIG_LANGUAGE)) {
                propKey = StringUtils.replaceOnce(key,
                        Constants.PROPRTY_KEY_SUB_STRING, ".en.");
            }
            value = getProperty(propKey);
            return value;
        } else {
            return getProperty(key);
        }
    }

    /**
     * @param key
     *            - String.
     * @return property value.
     */
    public final String getProperty(final String key) {

        if (appsConfigProperties.containsKey(key)) {
            LOG.debug("Found {} in cache, returning value from cache", key);
            return appsConfigProperties.get(key).toString();
        } else {
            LOG.debug("Did not find {} in cache, getting from server", key);
        }
        Session session = null;

        String keyPath = null;
        String tempKeyPath = null;
        String replaceDot = null;
        String[] tempKey = key.split("\\.");

        // if the length of array is less than two, then it means that the
        // config collection and key
        // combination is not passed correctly, returning null
        if (tempKey.length < 2) {
            return null;
        }
        try {
            session = getSession();
            if (isValidLocale(tempKey[1])){
            	for(int i = 2; i < tempKey.length; i++){
            		replaceDot = key.replaceAll("\\.","/");
            		tempKeyPath = replaceDot.substring(0,replaceDot.lastIndexOf("/"));
            		tempKeyPath = tempKeyPath + Constants.FORWARD_SLASH + Constants.JCR_CONTENT_NODE ;
            		keyPath = tempKeyPath + replaceDot.substring(replaceDot.lastIndexOf("/"));
            	}
            }
            else{
            	for(int i = 0; i < tempKey.length; i++){
            		replaceDot = key.replaceAll("\\.","/");
            		tempKeyPath = tempKey[0];
            		tempKeyPath = tempKeyPath + Constants.FORWARD_SLASH + Constants.DEFAULT_APPS_CONFIG_LANGUAGE; 
            		tempKeyPath = tempKeyPath + replaceDot.substring(replaceDot.indexOf("/"), replaceDot.lastIndexOf("/"));
            		tempKeyPath = tempKeyPath + Constants.FORWARD_SLASH + Constants.JCR_CONTENT_NODE ;
            		keyPath = tempKeyPath + replaceDot.substring(replaceDot.lastIndexOf("/"));
            	}
            }
            try {
                final String runModes = System.getProperty("sling.run.modes");
                final String[] modes = StringUtils.split(runModes,
                        Constants.COMMA);
                String instance = Constants.AUTHOR;

                if (modes != null) {
                    LOG.debug("Instance: " + modes[0]);
                    instance = modes[0];
                }
                String propVal = null;
                Node configNode = session
                        .getNode(Constants.APPS_CONFIG_PATH);
                
                // remove the forward slash at the start.
                if (StringUtils.startsWith(keyPath, "/")) {
                	keyPath = keyPath.substring(1);
                }                
                LOG.info("KeyPath : {}", keyPath);

               
                
                if (configNode.hasNode(keyPath)) {
                	configNode = configNode.getNode(keyPath);
                    try {
                        if ((StringUtils.equalsIgnoreCase(instance,
                                Constants.PUBLISH))) {
                            propVal = configNode.getProperty(instance).getValue()
                                    .getString();
                            LOG.debug("propVal publish:" + propVal);
                       }
                        if ((StringUtils.equalsIgnoreCase(instance,
                                Constants.AUTHOR))) {
                            propVal = configNode.getProperty(instance).getValue()
                                    .getString();

                            if (propVal == null || StringUtils.isEmpty(propVal)) {

                                propVal = configNode.getProperty(Constants.PUBLISH)
                                        .getValue().getString();

                            }
                        }
                    } catch (Exception e) {
                        LOG
                                .error(
                                        "Exception in getProperty method of " +
                                        "AppsConfigServiceImpl",
                                        e);
                    }

                    LOG.debug("putting in cache {} = {}", key, propVal);
                    appsConfigProperties.put(key, propVal);
                    return propVal;
                } else {
                	LOG.info("KeyPath does not exist.");
                	return null;
                }
                

            } catch (Exception e) {
                LOG
                        .error(
                                "Exception in getProperty method of " +
                                "AppsConfigServiceImpl",
                                e);
                return null;
            }
        } catch (GenericException e1) {
            LOG
                    .error(
                            "GenericException in getProperty method of" +
                            " AppsConfigServiceImpl",
                            e1);
            return null;
        } finally {
            if (session != null) {
                // session.logout();
            }
        }
    }

    /**
     * Transform the htmContent by substituting the property place holders with
     * the corresponding property values.
     * 
     * @param htmlContent
     *            - The html content to be transformed.
     * @return - The transformed html content as string.
     */
    public final String transformContent(final String htmlContent) {
        return transformContent(null, htmlContent, null);
    }

    /**
     * Gets the repository.
     * 
     * @return the repository
     */
    public final SlingRepository getRepository() {
        return repository;
    }

    /**
     * return true if it is a valid locale value.
     * 
     * @param value
     *            - The local string to check.
     * @return - true/false.
     */
    private boolean isValidLocale(final String value) {
        final Locale[] locales = Locale.getAvailableLocales();
        for (Locale l : locales) {
            if (value.equals(l.toString())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Activate.
     * 
     * @param componentContext
     *            - the component context.
     */
    @SuppressWarnings("unchecked")
    protected final void activate(final ComponentContext componentContext) {
        LOG.info("inside activate of {}", this.getClass());
        final Dictionary<String, String> properties = componentContext
                .getProperties();
        period = properties.get(Constants.PERIOD);
        // case : with addPeriodicJob(): executes the job every 1 minutes
        String jobName = Constants.SYNC_APPS_CONFIG;
        boolean canRunConcurrently = false;
        long periodLong = period != null ? Long.parseLong(period) : 60;
        Map<String, Serializable> config = new HashMap<String, Serializable>();
        final Runnable appsConfigJob = new Runnable() {
            public void run() {
                syncProperties();
            }
        };
        try {
            this.scheduler.addPeriodicJob(jobName, appsConfigJob, config,
                    periodLong, canRunConcurrently);
        } catch (Exception e) {
            LOG.error("Error in scheduling {} " + Constants.SYNC_APPS_CONFIG);
        }

    }

    /** The admin session. */
    private Session adminSession = null;

    /**
     * Gets the session.
     * 
     * @return the session
     * @throws GenericException
     *             the application exception
     */
    protected final Session getSession() throws GenericException {

        try {
            if (adminSession == null) {
                adminSession = repository.loginAdministrative(null);
            }
            return adminSession;

        } catch (RepositoryException re) {
            throw new GenericException("Error in getSession of BaseManagerImpl",
                    re);

        }
    }

    /**
     * Refreshes the appsConfigProperties.
     * 
     * @return void.
     */
    private void syncProperties() {
        LOG.debug("Synchronizeing appsConfig cache");
        Iterator<String> keysItr = appsConfigProperties.keySet().iterator();
        List<String> keyList = new ArrayList<String>();
        while (keysItr.hasNext()) {
            String key = keysItr.next();
            keyList.add(key);
        }
        for (int i = 0; i < keyList.size(); i++) {
            appsConfigProperties.remove(keyList.get(i));
        }

        for (int i = 0; i < keyList.size(); i++) {
            getProperty(keyList.get(i));
        }
        LOG.debug("Synchronized appsConfig cache");
    }

    /**
     * De-activate this component.
     * 
     * @param context
     *            - The component context.
     */
    protected void deactivate(ComponentContext context) {
        LOG.debug("entering deactivate methd of {}", this.getClass());
        this.scheduler.removeJob(Constants.SYNC_APPS_CONFIG);
        LOG.debug("Removed " + Constants.SYNC_APPS_CONFIG);
        LOG.debug("leaving deactivate methd of {}", this.getClass());
    }
    /**
     * Transform content.
     * 
     * @param locale
     *            the locale
     * @param htmlContent
     *            the html content
     * @param mockValues
     *            - The mock values to be used.
     * @return the string
     */
    public String transformContent(Locale locale, final String htmlContent,
            Map<String, String> mockValues, boolean isEditMode) {
        if (StringUtils.isEmpty(htmlContent)) {
            return htmlContent;
        }
        Pattern pattern = Pattern.compile("\\{(.+?)\\}");
        Matcher matcher = pattern.matcher(htmlContent);
        StringBuffer buffer = new StringBuffer();
        String replacement;
        while (matcher.find()) {
            replacement = getProperty(locale, matcher.group(1), mockValues);
            if (replacement != null) {
                matcher.appendReplacement(buffer, Constants.EMPTY_STRING);
                buffer.append(replacement);
            }
        }
        matcher.appendTail(buffer);

        LOG.debug("parsed content : " + buffer.toString() );
        return buffer.toString();
    }

    

    /**
     * Transform the htmContent by substituting the property place holders with
     * the corresponding property values.
     * 
     * @param locale
     *            - The Locale
     * @param htmlContent
     *            - The html content to be transformed.
     * @return - The transformed html content as string.
     */
    public String transformContent(Locale locale, String htmlContent) {
        return transformContent(locale, htmlContent, null);
    }

    /**
     * Bind repository.
     * 
     * @param repo
     *            the SlingRepository
     */
    protected final void bindRepository(final SlingRepository repo) {
        this.repository = repo;
    }

    /**
     * Unbind repository.
     * 
     * @param repo
     *            the SlingRepository
     */
    protected final void unbindRepository(SlingRepository repo) {
        if (this.repository == repo) {
            repo = null;
        }
    }

    /**
     * For creating node in appsconfig
     * 
     * @param appsConfigBean
     * @param path
     * @throws GenericException
     */
    public void saveProperty(AppsConfigBean appsconfigBean, String path)
            throws GenericException {

        LOG.info("entering setProperty method of AppsConfigServiceImpl");

        Session session = getSession();

        String nodepath = path + Constants.FORWARD_SLASH
                + Constants.JCR_CONTENT_NODE;

        LOG.info("Path in saveProperty" + nodepath);

        try {
            Node node = session.getNode(nodepath);

            LOG.info("Node in saveProperty" + node);

            String nodeName = appsconfigBean.getPropertyName();

            LOG.info("nodeName in saveProperty" + nodeName);
            Node appsConfigNode;
            String existingNode = appsconfigBean.getNodeName();
            LOG.info("node exists" + existingNode);
            if (StringUtils.isEmpty(existingNode)) {

                appsConfigNode = node.addNode(nodeName,
                        Constants.JCR_NT_UNSTRUCTURED);
            } else {
                appsConfigNode = session.getNode(existingNode);
            }

            JcrUtil.setProperty(appsConfigNode, Constants.AUTHOR,
                    appsconfigBean.getAuthorValue());
            JcrUtil.setProperty(appsConfigNode, Constants.PUBLISH,
                    appsconfigBean.getPublishValue());
            session.save();

        } catch (RepositoryException e) {
            throw new GenericException("Error while saving appsConfignode", e);
        }
        LOG.info("saved appsconfig node");

    }

    /**
     * Gets the all appsconfig nodes.
     * 
     * @param path
     * @return
     * @throws GenericException
     */
    public List<AppsConfigBean> getAllProperties(String path,Session session)
            throws GenericException {

        LOG.info("entering getAllNodes method of AppsConfigServiceImpl");
        List<AppsConfigBean> appsConfigList = new ArrayList<AppsConfigBean>();
        if (session == null) {
            session = getSession();
        }

        path = path + Constants.FORWARD_SLASH + Constants.JCR_CONTENT_NODE;

        try {
            Node node = session.getNode(path);
            NodeIterator nodeIterator = node.getNodes();
            Node appConfigNode;
            AppsConfigBean appsConfigBean;
            while (nodeIterator.hasNext()) {
                appConfigNode = (Node) nodeIterator.next();
                appsConfigBean = new AppsConfigBean();
                appsConfigBean.setPath(appConfigNode.getPath());
                appsConfigBean.setPropertyName(appConfigNode.getName());

                if (appConfigNode.hasProperty(Constants.AUTHOR)) {
                    appsConfigBean.setAuthorValue(appConfigNode.getProperty(
                            Constants.AUTHOR).getString());
                }
                if (appConfigNode.hasProperty(Constants.PUBLISH)) {
                    appsConfigBean.setPublishValue(appConfigNode.getProperty(
                            Constants.PUBLISH).getString());
                }
                appsConfigList.add(appsConfigBean);
            }

        } catch (RepositoryException e) {
            throw new GenericException("Error while retreiving appsconfig", e);
        }

        return appsConfigList;
    }

    /**
     * Checks if it is duplicate.
     * 
     * @param propertyName
     * @param path
     * @return
     */
    public boolean isDuplicate(String propertyName, String path) {

        LOG.info("entering isDuplicate method of AppsConfigServiceImpl");

        path = path + Constants.FORWARD_SLASH + Constants.JCR_CONTENT_NODE;

        boolean result = false;
        try {
            Session session = getSession();
            QueryManager qm = session.getWorkspace().getQueryManager();
            StringBuffer xpath = new StringBuffer(Constants.JCR_ROOT).append(
                    path).append(Constants.FORWARD_ASTERISK);
            LOG.debug(xpath.toString());
            Query q = qm.createQuery(xpath.toString(), Constants.XPATH);
            LOG.debug("running query....");
            QueryResult qr = q.execute();
            long size = qr.getRows().getSize();
            LOG.debug("results.size() = " + size);

            if (size != 0) {
                NodeIterator nItr = qr.getNodes();
                while (nItr.hasNext()) {
                    Node n = nItr.nextNode();
                    if (propertyName.equals(n.getName())) {
                        result = true;
                        break;
                    } else {
                        result = false;
                    }
                }
            }
        } catch (RepositoryException e) {
            LOG
                    .error(
                            "RepositoryException in isDuplicate method of " +
                            "AppsConfigServiceImpl",
                            e);
        } catch (GenericException e) {
            LOG
                    .error(
                            "GenericException in isDuplicate method of " +
                            "AppsConfigServiceImpl",
                            e);
        } finally {

        }
        return result;
    }

    /**
     * For publishing appsConfig Nodes.
     * 
     * @param publishList
     * @return status
     */
    public boolean publishNodes(Session session, String publishList)
            throws AccessDeniedException {
        LOG.debug("AppsConfigServiceImpl Strarting of publishNodes Method ");
        boolean status = false;

        try {
            if (session == null) {
                session = getSession();
            }
            Node node;
            if (publishList.contains(Constants.COMMA)) {
                StringTokenizer publishNodeList = new StringTokenizer(
                        publishList, Constants.COMMA);
                LOG.debug("AppsConfigServiceImpl multiples nodes selected "
                        + "for publish");
                while (publishNodeList.hasMoreTokens()) {
                    String nodeName = publishNodeList.nextToken();
                    if (nodeName != null) {

                        node = session.getNode(nodeName);
                        getReplicator().replicate(node.getSession(),
                                ReplicationActionType.ACTIVATE, node.getPath());
                        status = true;
                    }
                }
            } else {
                LOG.debug("AppsConfigServiceImpl only one single node is "
                        + "selected for publish");
                node = session.getNode(publishList);
                getReplicator().replicate(node.getSession(),
                        ReplicationActionType.ACTIVATE, node.getPath());
                status = true;
            }
        } catch (AccessDeniedException e) {
            LOG
                    .error(
                            "AccessDeniedException in publishNodes method of " +
                            "AppsConfigServiceImpl",
                            e);
            throw new AccessDeniedException(
                    "Error while publishing AppsConfigNodes", e);
        } catch (ReplicationException e) {
            LOG
                    .error(
                            "ReplicationException in publishNodes method of" +
                            " AppsConfigServiceImpl",
                            e);
            return false;
        } catch (RepositoryException e) {
            LOG
                    .error(
                            "RepositoryException in publishNodes method of " +
                            "AppsConfigServiceImpl",
                            e);
            return false;
        } catch (GenericException e) {
            LOG
                    .error(
                            "GenericException in publishNodes method of " +
                            "AppsConfigServiceImpl",
                            e);
            return false;
        }

        return status;

    }
	
	/**
	 * For publishing All appsConfig Nodes.
	 * 
	 * @param path
	 * @return status
	 */
	public boolean publishAllNodes(Session session, String path)
			throws AccessDeniedException {
		boolean status = true;
		try {
			LOG.debug("publishAllNodes path" + path);
			Node node = session.getNode(path);
			NodeIterator nodeIterator = node.hasNodes() ? node.getNodes()
					: null;
			while (nodeIterator != null && nodeIterator.hasNext()) {
				Node childNode = nodeIterator.nextNode();
				publishAllNodes(session, childNode.getPath());
			}
			LOG.debug("Publishing node path:" + node.getPath());
			getReplicator().replicate(node.getSession(),
					ReplicationActionType.ACTIVATE, node.getPath());
		} catch (RepositoryException e) {
			LOG.error("RepositoryException in publishNodes method of "
					+ "AppsConfigServiceImpl", e);
			return false;
		} catch (ReplicationException e) {
			status = false;
			LOG.error("ReplicationException in publishNodes method of"
					+ " AppsConfigServiceImpl", e);
			return false;
		}
		return status;
	}

	/**
	 * Provides the path of the publishing node
	 * 
	 * @param session
	 * @param path
	 * @return nodePath
	 */
	public String getAppsConfigCollectionPath(Session session, String path) {
		String nodePath = null;
		try {
			LOG.debug("In getPublishAllPath method ");
			Node tempNode = session.getNode(path);
			if (tempNode != null) {
				if (tempNode.getPrimaryNodeType().getName().equalsIgnoreCase(
						"cq:Page")) {
					NodeIterator nodeIterator = tempNode.getNodes();
					Node childNode;
					if (nodeIterator != null) {
						while (nodeIterator.hasNext()) {
							childNode = nodeIterator.nextNode();
							if (childNode.getName().equalsIgnoreCase(
									"jcr:content")) {
								String propNode = childNode
										.hasProperty("cq:template") ? childNode
										.getProperty("cq:template").getValue()
										.getString() : null;
								if (propNode != null) {
									if (propNode
											.equalsIgnoreCase("/apps/template/templates/appsconfigcollection")) {
										nodePath = tempNode.getPath();
										LOG.debug("tempPath" + nodePath);
										return nodePath;
									} else {
										nodePath = getAppsConfigCollectionPath(
												session, tempNode.getParent()
														.getPath());
										return nodePath;
									}
								}
							}
						}
					}
				}
			}
		} catch (PathNotFoundException e) {
			LOG.error("PathNotFoundException in getPublishAllPath", e);
		} catch (RepositoryException re) {
			LOG.error("RepositoryException in getPublishAllPath", re);
		}
		return nodePath;
	}

    /**
     * Gets the scheduler.
     * 
     * @return the scheduler
     */
    public Scheduler getScheduler() {
        return scheduler;
    }

    /**
     * Sets the scheduler.
     * 
     * @param scheduler
     *            the new scheduler
     */
    protected void setScheduler(Scheduler scheduler) {
        this.scheduler = scheduler;
    }

    /**
     * Un set scheduler.
     * 
     * @param scheduler
     *            the scheduler
     */
    protected void unSetScheduler(Scheduler scheduler) {
        if (this.scheduler == scheduler) {
            this.scheduler = null;
        }

    }

    /**
     * Transform content.
     * 
     * @param locale
     *            the locale
     * @param htmlContent
     *            the html content
     * @param mockValues
     *            - The mock values to be used.
     * @return the string
     */
    public String transformContent(Locale locale, String htmlContent,
            Map<String, String> mockValues) {
        return transformContent(locale, htmlContent, mockValues, false);
        
    }

}