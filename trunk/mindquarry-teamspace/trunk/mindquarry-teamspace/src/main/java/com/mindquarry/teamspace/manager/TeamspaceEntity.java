/**
 * Copyright (C) 2006 Mindquarry GmbH, All Rights Reserved
 */
package com.mindquarry.teamspace.manager;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.mindquarry.common.persistence.EntityBase;
import com.mindquarry.teamspace.Teamspace;
import com.mindquarry.teamspace.UserRO;


/**
 * Add summary documentation here.
 *
 * @author 
 * <a href="mailto:bastian.steinert(at)mindquarry.com">your full name</a>
 */
public class TeamspaceEntity extends EntityBase implements Teamspace {

    private String name;
    private String description;
    private String workspaceUri;
    private Set<UserRO> users;

    
    /**
     * 
     */
    public TeamspaceEntity() {
        this.id = "".intern();
        this.name = "".intern();
        this.description = "".intern();
        this.workspaceUri = "".intern();
        this.users = new HashSet<UserRO>();
    }

    /**
     * @param id
     * @param name
     * @param description
     * @param workspaceUri
     */
    public TeamspaceEntity(String id, String name, String description, 
            String workspaceUri, Set<UserRO> users) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.workspaceUri = workspaceUri;
        this.users = users;
    }
    
    /**
     * @see com.mindquarry.types.teamspace.Teamspace#getDescription()
     */
    public String getDescription() {
        return description;
    }
    /**
     * @see com.mindquarry.types.teamspace.Teamspace#setDescription(java.lang.String)
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @see com.mindquarry.types.teamspace.Teamspace#getName()
     */
    public String getName() {
        return name;
    }
    /**
     * @see com.mindquarry.types.teamspace.Teamspace#setName(java.lang.String)
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @see com.mindquarry.types.teamspace.Teamspace#getWorkspaceUri()
     */
    public String getWorkspaceUri() {
        return workspaceUri;
    }
    /**
     * @see com.mindquarry.types.teamspace.Teamspace#setWorkspaceUri(java.lang.String)
     */
    public void setWorkspaceUri(String workspaceUri) {
        this.workspaceUri = workspaceUri;
    }
    
    /**
     * @see com.mindquarry.teamspace.TeamspaceRO#getUsers()
     */
    public List<UserRO> getUsers() {
        List<UserRO> userList = new LinkedList<UserRO>(users);   
        return Collections.unmodifiableList(userList);
    }
    
    void setUsers(Set<UserRO> value) {
        this.users = value;
    }
    
    void addUser(UserRO user) {
        this.users.add(user);
    }
}