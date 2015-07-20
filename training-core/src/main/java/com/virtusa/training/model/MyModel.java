package com.virtusa.training.model;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.Model;
@Model(adaptables=Resource.class)
public interface MyModel {
	@Inject @Named("name")
    public String getName();
}
