/*
 * Copyright (C) 2006-2007 Mindquarry GmbH, All Rights Reserved
 * 
 * The contents of this file are subject to the Mozilla Public License
 * Version 1.1 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 */
package com.mindquarry.teamspace.manager;

import static com.mindquarry.user.manager.DefaultUsers.isAdminUser;
import static com.mindquarry.user.manager.DefaultUsers.isAnonymousUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.mindquarry.auth.AuthorizationAdmin;
import com.mindquarry.auth.ProfileRO;
import com.mindquarry.auth.RightRO;
import com.mindquarry.common.resources.ResourceDoesNotExistException;
import com.mindquarry.persistence.api.Session;
import com.mindquarry.persistence.api.SessionFactory;
import com.mindquarry.teamspace.Authorisation;
import com.mindquarry.teamspace.CouldNotCreateTeamspaceException;
import com.mindquarry.teamspace.CouldNotRemoveTeamspaceException;
import com.mindquarry.teamspace.Membership;
import com.mindquarry.teamspace.Teamspace;
import com.mindquarry.teamspace.TeamspaceAdmin;
import com.mindquarry.teamspace.TeamspaceRO;
import com.mindquarry.user.GroupRO;
import com.mindquarry.user.User;
import com.mindquarry.user.UserRO;
import com.mindquarry.user.manager.UserManager;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:bastian.steinert(at)mindquarry.com">Bastian Steinert</a>
 */
public final class TeamspaceManager implements TeamspaceAdmin, Authorisation {

    static final String ADMIN_USER_ID = "admin";

    static final String ADMIN_PWD = "admin";

    static final String ADMIN_NAME = "Administrator";

    private SessionFactory sessionFactory_;

    private DefaultListenerRegistry listenerRegistry_;

    private UserManager userManager_;

    private AuthorizationAdmin authAdmin_;

    /**
     * Setter for listenerRegistry bean, set by spring at object creation
     */
    public void setListenerRegistry(DefaultListenerRegistry listenerRegistry) {
        listenerRegistry_ = listenerRegistry;
    }

    /**
     * Setter for sessionFactory bean, set by spring at object creation
     */
    public void setSessionFactory(SessionFactory sessionFactory) {
        sessionFactory_ = sessionFactory;
    }

    /**
     * Setter for userManager bean, set by spring at object creation
     */
    public void setUserManager(UserManager userManager) {
        userManager_ = userManager;
    }

    /**
     * Setter for authAdmin bean, set by spring at object creation
     */
    public void setAuthAdmin(AuthorizationAdmin authAdmin) {
        authAdmin_ = authAdmin;
    }

    public Teamspace createTeamspace(String teamspaceId, String name,
            String description, UserRO teamspaceCreator)
            throws CouldNotCreateTeamspaceException {
        
        TeamspaceEntity teamspace = new TeamspaceEntity();
        teamspace.setId(teamspaceId);
        teamspace.setName(name);
        teamspace.setDescription(description);
        
        // there is no need to make the admin a member of the teamspace,
        // at most its somewhat confusing if the admin user appears as member
        // within the teamspace views
        // Nevertheless she/he is not only allowed to create and teamspaces
        // but also to edit existing ones.
        if (! isAdminUser(teamspaceCreator)) {
            teamspace.addUser(teamspaceCreator);
        }

        Session session = currentSession();
        session.persist(teamspace);
        session.commit();
        
        // create the teams default group
        createGroupForTeam(teamspace);

        try {
            listenerRegistry_.signalBeforeTeamspaceCreated(teamspace);
        } catch (Exception e) {
            throw new CouldNotCreateTeamspaceException(
                    "Teamspace creation failed in listener: " + e.getMessage(),
                    e);
        }
        
        return teamspace;
    }
    
    private void createGroupForTeam(TeamspaceRO teamspace) {
        GroupRO teamGroup = userManager_.createGroup(teamspace.getId());

        for (UserRO teamMember : teamspace.getUsers())
            userManager_.addUser(teamMember, teamGroup);

        String teamspaceUri = "/teamspaces/" + teamspace.getId();
        RightRO rRight = authAdmin_.createRight(teamspaceUri, "READ");
        RightRO wRight = authAdmin_.createRight(teamspaceUri, "WRITE");

        String profileName = teamspace.getId() + "-user";
        ProfileRO profile = authAdmin_.createProfile(profileName);
        authAdmin_.addRight(rRight, profile);
        authAdmin_.addRight(wRight, profile);

        authAdmin_.addAllowance(profile, teamGroup);
    }
    
    private void deleteGroupForTeam(TeamspaceRO teamspace) {
        GroupRO group = userManager_.groupById(teamspace.getId());
        userManager_.deleteGroup(group);
    }

    public void updateTeamspace(Teamspace teamspace) {
        Session session = currentSession();
        session.update(teamspace);
        session.commit();
    }

    public void deleteTeamspace(Teamspace teamspace)
            throws CouldNotRemoveTeamspaceException {

        try {
            listenerRegistry_.signalAfterTeamspaceRemoved(teamspace);
        } catch (Exception e) {
            throw new CouldNotRemoveTeamspaceException(
                    "Teamspace removal failed in listener " + e.getMessage(), e);
        }
        
        deleteGroupForTeam(teamspace);
        
        for (UserRO user : teamspace.getUsers()) {
            removeMember((User) user, teamspace);            
        }
        
        Session session = currentSession();
        session.delete(teamspace);
        session.commit();
    }

    public Collection<? extends TeamspaceRO> teamspacesForUser(String userId) {
        assert userManager_.isValidUserId(userId) : "the userId: " + userId
                + " is not valid";

        Collection<? extends TeamspaceRO> result;
        
        Session session = currentSession();
        if (ADMIN_USER_ID.equals(userId))
            result = queryAllTeamspaces();
        else
            result = queryTeamspacesForUser(userId);
        session.commit();
        
        return result;
    }

    private List<TeamspaceRO> queryAllTeamspaces() {
        Session session = currentSession();

        List<Object> queriedTeamspaces = session.query("getAllTeams",
                new Object[0]);

        List<TeamspaceRO> result = new LinkedList<TeamspaceRO>();
        for (Object teamspaceObj : queriedTeamspaces) {
            TeamspaceEntity teamspace = (TeamspaceEntity) teamspaceObj;
            result.add(teamspace);
        }

        session.commit();
        return result;
    }

    private List<? extends TeamspaceRO> queryTeamspacesForUser(String userId) {
        Session session = currentSession();

        List<Object> queriedTeams = session.query(
                "getTeamsForUser", new Object[] {userId});
        
        List<TeamspaceEntity> teams = new LinkedList<TeamspaceEntity>();
        for (Object object : queriedTeams) {
            teams.add((TeamspaceEntity) object);
        }
        return teams;
    }

    /**
     * @see com.mindquarry.teamspace.TeamspaceQuery#teamspaceById(java.lang.String)
     */
    public Teamspace teamspaceById(String teamspaceId) {        
        return queryTeamspaceById(teamspaceId);
    }

    /**
     * @returns a teamspace object if it can be found otherwise null
     */
    private TeamspaceEntity queryTeamspaceById(String id) {
        Session session = currentSession();
        TeamspaceEntity result = null;
        List queryResult = session.query("getTeamById",
                new Object[] { id });
        if (queryResult.size() == 1) {
            result = (TeamspaceEntity) queryResult.get(0);
        }
        return result;
    }

    private Session currentSession() {
        return sessionFactory_.currentSession();
    }
    
    public void addMember(User user, Teamspace team) {
        ((TeamspaceEntity) team).addUser(user);
        
        Session session = currentSession();
        session.update(team);
        session.commit();
    }
    
    public void removeMember(User user, Teamspace team) {
        ((TeamspaceEntity) team).removeUser(user);
        
        Session session = currentSession();
        session.update(team);
        session.commit();
    }

    
    public Membership membership(TeamspaceRO teamspace) {
        List<UserRO> users = userManager_.allUsers();
        Set<UserRO> members = new HashSet<UserRO>();
        Set<UserRO> nonMembers = new HashSet<UserRO>();

        for (UserRO user : users) {
            if (teamspace.getUsers().contains(user))
                members.add(user);
            else
                nonMembers.add(user);
        }

        return new Membership(teamspace, members, nonMembers);
    }

    public Membership refreshMembership(Membership oldMembership) {
        
        Membership result = membership(oldMembership.teamspace);
        for (UserRO user : oldMembership.getAddedMembers()) {
            result.addMember(user);
        }
        for (UserRO user : oldMembership.getRemovedMembers()) {
            result.removeMember(user);
        }
        return result;
    }

    // TODO think about requerying or reattaching of users and teamspaces
    // to new session, because most of the times the object was queried
    // in transactions and sessions before the current one
    public Membership updateMembership(Membership membership) {
        
        // check old members for removal from teamspace
        for (UserRO user : membership.getRemovedMembers()) {
            removeMember((User) user, (Teamspace) membership.teamspace);
        }

        // check new members for adding to teamspace
        for (UserRO user : membership.getAddedMembers()) {
            addMember((User) user, (Teamspace) membership.teamspace);
        }

        // return a new calculated membership
        return membership(membership.teamspace);
    }

    /**
     * Simple authorisation implementation. Considers 'userId' and 'uri',
     * 'method' is completely ignored.
     * 
     * <ul>
     * <li>'admin' can do anything</li>
     * <li>all other users can do anything in the teams they belong to</li>
     * <li><code>ResourceNotFoundException</code> if the uri contains a
     * non-existing team</li>
     * </ul>
     * 
     * The uri containing the team-id that a user might belong to should have
     * the form <code>jcr:///teamspaces/&lt;team-id&gt;/[sub-resource]</code>.
     * If the uri is different, only the 'admin' will be allowed to do
     * something.
     * 
     */
    public boolean authorise(String userId, String uri, String method) {
        
        UserRO user = userManager_.userById(userId);
        if (isRequestForTeamsContent(uri))
            return authorizeTeamsContent(uri, user);
        else if (isRequestForUserManagement(uri))
            return authorizeUserManagement(uri, user);
        else
            return false;
    }
    
    private boolean isRequestForTeamsContent(String uri) {
        return uri.startsWith("jcr:///teamspaces");
    }
    
    // simple regular expression that looks for the teamspace name...
    private static final Pattern teamContentPattern = 
                Pattern.compile("jcr:///teamspaces/([^/]*)/(.*)");
    
    private boolean authorizeTeamsContent(String uri, UserRO user) {
        if (isAdminUser(user)) {
            return true;
        }
        
        Matcher m = teamContentPattern.matcher(uri);
        if (m.matches()) {
            String requestedTeamspaceID = m.group(1); // "mindquarry";
            
            TeamspaceRO team = teamspaceById(requestedTeamspaceID);
            // ...check if that teamspace exists
            if (team == null) {
                throw new ResourceDoesNotExistException(uri, "Teamspace '"
                        + requestedTeamspaceID + "' does not exist.");
            }

            if (team.getUsers().contains(user)) {
                return true;
            }
        }
        return false;            
    }
    
    private boolean isRequestForUserManagement(String uri) {
        return uri.startsWith("jcr:///users");
    }
    
    private boolean authorizeUserManagement(String uri, UserRO user) {
        if (uri.contains("editUser")) {
            return  ! isAnonymousUser(user);
        }
        else
            return isAdminUser(user);
    }
}
