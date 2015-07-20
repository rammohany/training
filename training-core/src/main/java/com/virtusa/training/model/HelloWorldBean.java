/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.model;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
@Model(adaptables={Resource.class})
public class HelloWorldBean implements Serializable {

    private static final long serialVersionUID = 1L;
    @Inject @Named("name")
    private String message;

    private String appConfigMessage;

    public HelloWorldBean() {

    }
   /* @Inject
    public HelloWorldBean(@Named("name") String name) {
    	this.message = name;
    }*/
    public HelloWorldBean(String message, String appConfigMessage) {
        super();
        this.message = message;
        this.appConfigMessage = appConfigMessage;
    }
    //@Inject @Named("name")
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getAppConfigMessage() {
        return appConfigMessage;
    }

    public void setAppConfigMessage(String appConfigMessage) {
        this.appConfigMessage = appConfigMessage;
    }

    @Override
    public String toString() {
        return "HelloWorldBean [message=" + message + ", appConfigMessage="
                + appConfigMessage + "]";
    }

}
