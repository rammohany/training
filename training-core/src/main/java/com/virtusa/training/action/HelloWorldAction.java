/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.action;

import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.jcr.ValueFormatException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.virtusa.training.model.HelloWorldBean;

public class HelloWorldAction extends BaseAction {

    private Logger log =  LoggerFactory.getLogger(HelloWorldAction.class);

    public HelloWorldBean helloWorld() {
        HelloWorldBean helloWorldBean = new HelloWorldBean();
        try {
            helloWorldBean.setMessage(getCurrentNode().getProperty("name").getString());
        } catch (ValueFormatException e) {
            log.error(e.getMessage(), e);
        } catch (PathNotFoundException e) {
            log.error(e.getMessage(), e);
        } catch (RepositoryException e) {
            log.error(e.getMessage(), e);
        }

        return helloWorldBean;
    }

}
