/**
 * Copyright (C) 2014 Virtusa Corporation.
 * This file is proprietary and part of Virtusa LaunchPad.
 * LaunchPad code can not be copied and/or distributed without the express permission of Virtusa Corporation
 */

package com.virtusa.training.taglib;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.TagSupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * ActionTag class that extends TagSupport.
 *
 */

public class ActionTag extends TagSupport {

    
	/**
	 * 
	 */
	private static final long serialVersionUID = 5149668215451508385L;

	/**
     * bean.
     */
    private String bean;

    /**
     * interfaceName.
     */
    private String actionClassName;
    
    /**
     * actionName.
     */
    private String actionName;
    
    private static HashMap<String,Object> classInstance=new HashMap<String, Object>();

    public String getActionName() {
		return actionName;
	}

	public void setActionName(String actionName) {
		this.actionName = actionName;
	}

	/**
     * logger object to set the LOG messages.
     */
    private static final Logger LOG = LoggerFactory
            .getLogger(ActionTag.class);


    /**
     * This method will be executed at the end of the tag. it will call the base
     * action class execute method.
     *
     * @return true/false
     */
    public final int doEndTag() { 	
    	try {
            final ClassLoader tccl = Thread.currentThread()
                    .getContextClassLoader();
            
            Class<?> cls;
            cls = Class.forName(this.actionClassName, true, tccl);
            final Class<?>[] params = new Class[1];
            String className=cls.getName();
            params[0] = PageContext.class;
            Object obj;
            if(classInstance.containsKey(className))
            {
            	obj=classInstance.get(className);
            }
            else
            {
            	obj = cls.newInstance();
            	classInstance.put(className, obj);
            	
            }
            final Object[] arglist = new Object[1];
            arglist[0] = this.pageContext;
            Method initMethod;
            initMethod = cls.getMethod("init", params);
            initMethod.invoke(obj, arglist);
            Method meth;
            meth = cls.getMethod(actionName);
            Object retobj;
            retobj = meth.invoke(obj);
            pageContext.setAttribute(this.bean, retobj);
        }
        catch (ClassNotFoundException e) {
            LOG.error("ClassNotFoundException : ", e);
        }
        catch (IllegalArgumentException e) {
            LOG.error("IllegalArgumentException : ", e);
        }
        catch (IllegalAccessException e) {
            LOG.error("IllegalAccessException : ", e);
        }
        catch (InvocationTargetException e) {
            LOG.error("InvocationTargetException : ", e);
        }
        catch (SecurityException e) {
            LOG.error("SecurityException : ", e);
        }
        catch (NoSuchMethodException e) {
            LOG.error("NoSuchMethodException : ", e);
        }
        catch (InstantiationException e) {
            LOG.error("InstantiationException : ", e);
        }

        return 0;
    }

    /**
     * to set the bean.
     *
     * @param beanArg
     *            - bean.
     */
    public final void setBean(final String beanArg) {
        this.bean = beanArg;
    }

    /**
     * to get the bean.
     *
     * @return bean.
     */
    public final String getBean() {
        return bean;
    }

    /**
     * To set the class name.
     *
     * @param classNameArg
     *            - className.
     */
    public final void setActionClassName(final String classNameArg) {
        this.actionClassName = classNameArg;
    }

    /**
     * To get the className.
     *
     * @return className.
     */
    public final String getActionClassName() {
        return actionClassName;
    }
        
}