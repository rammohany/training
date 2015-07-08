package com.virtusa.training.model;

import java.io.Serializable;

/**
 * AppsConfigBean
 * 
 * 
 */
public class AppsConfigBean extends BaseBean implements Serializable {
	/**
	 * To hold Serial Version UID.
	 */
	private static final long serialVersionUID = 1L;

	private String propertyName;

	private String authorValue;

	private String publishValue;

	private String path;

	private String nodeName;

	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public String getPropertyName() {
		return propertyName;
	}

	public void setPropertyName(String propertyName) {
		this.propertyName = propertyName;
	}

	public String getAuthorValue() {
		return authorValue;
	}

	public void setAuthorValue(String authorValue) {
		this.authorValue = authorValue;
	}

	public String getPublishValue() {
		return publishValue;
	}

	public void setPublishValue(String publishValue) {
		this.publishValue = publishValue;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return null;
	}

}
