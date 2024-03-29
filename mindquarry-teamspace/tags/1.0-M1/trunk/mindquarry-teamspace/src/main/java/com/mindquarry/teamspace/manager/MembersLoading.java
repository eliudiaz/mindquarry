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

import java.util.List;

import com.mindquarry.teamspace.TeamspaceRO;
import com.mindquarry.user.UserRO;

/**
 * Add summary documentation here.
 *
 * @author 
 * <a href="mailto:bastian.steinert(at)mindquarry.com">Bastian Steinert</a>
 */
class MembersLoading implements ListLoading<UserRO> {

    private TeamspaceRO teamspace_;
    
    private TeamspaceManager teamspaceManager_;
    
    /**
     * @param teamspace
     * @param teamspaceManager
     */
    public MembersLoading(TeamspaceRO teamspace, 
            TeamspaceManager teamspaceManager) {
        
        teamspace_ = teamspace;
        teamspaceManager_ = teamspaceManager;
    }

    /**
     * @see com.mindquarry.teamspace.manager.ListLoading#load()
     */
    public List<UserRO> load() {
        return teamspaceManager_.membersForTeamspace(teamspace_);
    }

}
