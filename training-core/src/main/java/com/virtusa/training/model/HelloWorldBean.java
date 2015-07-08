/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.model;

import java.io.Serializable;

public class HelloWorldBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String message;

    private String appConfigMessage;

    public HelloWorldBean() {

    }

    public HelloWorldBean(String message, String appConfigMessage) {
        super();
        this.message = message;
        this.appConfigMessage = appConfigMessage;
    }

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
