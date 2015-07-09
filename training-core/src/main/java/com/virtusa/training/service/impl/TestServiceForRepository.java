package com.virtusa.training.service.impl;

import javax.jcr.LoginException;
import javax.jcr.RepositoryException;
import javax.jcr.Session;

import org.apache.sling.jcr.api.SlingRepository;

public class TestServiceForRepository {
    protected SlingRepository repository;

    protected Session session;
    
    public void testMethod() {
    	try {
			session = repository.loginService(null, null);
		} catch (LoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RepositoryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// session.logout();
		}
    }

   
    protected void activate() throws Exception {
        
    }

   
    protected void deactivate() throws Exception {
        session.logout();
        session = null;
    }
}