package com.virtusa.training.test;

import javax.jcr.Node;
import javax.jcr.RepositoryException;

public class TestUtil {
	public static String getProperty(Node node, String propertyName) {
		try {
	        if (node.hasProperty(propertyName)) {
	            return node.getProperty(propertyName).getString();   
	        }
		} catch (RepositoryException re) {
			re.printStackTrace();
		}
        return null;
    }
}
