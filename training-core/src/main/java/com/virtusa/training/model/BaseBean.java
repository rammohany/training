package com.virtusa.training.model;

import java.io.Serializable;

/**
 * An abstract base model.
 * All model classes should extend this class.
 *
 */
public abstract class BaseBean implements Serializable {
    /**
     * serialVersionUID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * String representation of the object.
     * @return String - String representing this object.
     */
    public abstract String toString();
    /**
     * Hashcode of this object.
     * @return int - Integer value of the object.
     */
    public abstract int hashCode();
    /**
     * equals method of this object.
     * @param obj - The object to compare.
     * @return boolean - True/False.
     */
    public abstract boolean equals(Object obj);

}
