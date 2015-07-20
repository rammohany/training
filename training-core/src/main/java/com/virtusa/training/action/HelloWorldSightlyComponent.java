package com.virtusa.training.action;

import com.adobe.cq.sightly.WCMUse;
import com.virtusa.training.model.HelloWorldBean;
import com.virtusa.training.model.MyModel;

public class HelloWorldSightlyComponent extends WCMUse {
	private MyModel bean = null;
	private HelloWorldBean hwBean = null;
	@Override
	public void activate() throws Exception {
		System.out.println("inside HelloWorldSightlyComponent.activate " + getResource().getPath());
		bean = getResource().adaptTo(MyModel.class);
		hwBean = getResource().adaptTo(HelloWorldBean.class);
	}
	
	public HelloWorldBean getHelloWorldBean() {
		return this.hwBean;
	}
	
	public MyModel getMyModel() {
		return this.bean;
	}

}
