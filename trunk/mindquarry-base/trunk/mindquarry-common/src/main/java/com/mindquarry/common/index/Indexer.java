/**
 * Copyright (C) 2006 Mindquarry GmbH, All Rights Reserved
 */
package com.mindquarry.common.index;

import java.io.InputStream;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public interface Indexer {
    public static final String ROLE = Indexer.class.getName();
    
    public void index(InputStream content, String name, String location,
            String type);
}
