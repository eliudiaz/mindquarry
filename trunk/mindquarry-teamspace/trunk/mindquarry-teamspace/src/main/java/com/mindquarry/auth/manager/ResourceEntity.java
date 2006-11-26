/**
 * Copyright (C) 2006 Mindquarry GmbH, All Rights Reserved
 */
package com.mindquarry.auth.manager;

import java.util.HashMap;
import java.util.Map;

import com.mindquarry.auth.manager.RightSet;
import com.mindquarry.common.persistence.EntityBase;

/**
 * Add summary documentation here.
 *
 * @author 
 * <a href="mailto:bastian.steinert(at)mindquarry.com">Bastian Steinert</a>
 */
public final class ResourceEntity extends EntityBase {

    private String name;
    private RightSet rights;
    private Map<String, ResourceEntity> children; 
    
    public ResourceEntity() {
        this("", "");
    }
    
    public ResourceEntity(String id, String name) {
        this.id = id.replace('/', '_');
        this.name = name;
        this.rights = new RightSet();
        this.children = new HashMap<String, ResourceEntity>();
    }    
    
    /**
     * Getter for children.
     *
     * @return the children
     */
    public Map<String, ResourceEntity> getChildren() {
        return children;
    }

    /**
     * Setter for children.
     *
     * @param children the children to set
     */
    public void setChildren(Map<String, ResourceEntity> children) {
        this.children = children;
    }

    /**
     * Getter for name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Setter for name.
     *
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Getter for rights.
     *
     * @return the rights
     */
    public RightSet getRights() {
        return rights;
    }

    /**
     * Setter for rights.
     *
     * @param rights the rights to set
     */
    public void setRights(RightSet rights) {
        this.rights = rights;
    }

    boolean hasChildren() {
        return ! this.children.isEmpty();
    }
    
    void addRight(RightEntity right) {
        rights.add(right);
    }
    
    void removeRight(AbstractRight right) {
        rights.remove(right);
    }
    
    boolean hasRights() {
        return ! this.rights.isEmpty();
    }
    
    RightEntity rightForOperation(String operation) {
        return rights.rightForOperation(operation);
    }
    
    void addChild(ResourceEntity child) {
        children.put(child.name, child);
    }
    
    void removeChild(ResourceEntity child) {
        children.remove(child.name);
    }
    
    boolean hasChild(String name) {
        return this.children.containsKey(name);
    }
    
    ResourceEntity getChild(String name) {
        return this.children.get(name);
    }
}
