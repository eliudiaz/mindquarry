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
 
importClass(Packages.com.mindquarry.teamspace.TeamspaceQuery);
importPackage(Packages.com.mindquarry.user);
cocoon.load("flows/users.js");

function listTeamspacesForUser() {
    var teamspaceQuery = cocoon.getComponent(TeamspaceQuery.ROLE);
    
    var userId = cocoon.parameters["username"];
    var teamspaces = teamspaceQuery.teamspacesForUser(userId);
    
    var parameterMap = { "teamspaces" : teamspaces, "userId" : userById(userId) }
    var target = cocoon.parameters["target"];
    cocoon.sendPage(target, parameterMap);
}

function teamspacesByID() {
	var teamspaceQuery = cocoon.getComponent(TeamspaceQuery.ROLE);
    
    var id = cocoon.parameters["teamspaceID"];
    var teamspace = teamspaceQuery.teamspaceById(id);
    
    var parameterMap = { "teamspace" : teamspace }
    var target = cocoon.parameters["target"];
    cocoon.sendPage(target, parameterMap);
}

function showTeamspaceDetails() {
    var teamspaceQuery = cocoon.getComponent(TeamspaceQuery.ROLE);
    
    var id = cocoon.parameters["teamspaceID"];
    var teamspace = teamspaceQuery.teamspaceById(id);
    
    var parameterMap = { "teamspace" : teamspace }
    var target = cocoon.parameters["target"];
    cocoon.sendPage(target, parameterMap);
}
