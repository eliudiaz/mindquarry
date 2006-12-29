/*
 * Coypright (c) 2006 Mindquarry GmbH, Potsdam, Germany 
 */
package com.mindquarry.search.cocoon.filters;

import java.io.Reader;
import java.util.Map;

import org.apache.excalibur.source.Source;

public interface TextFilter {
	/**
	 * Use this for the full text
	 */
	public static final String CONTENT = "content";
    
    /**
     * Use this for the title, which will be used in search list results etc.
     */
    public static final String TITLE = "title";
    
//	public static final String ABSTRACT = "abstract";
//	public static final String AUTHOR = "author";
	
	/**
	 * The keys of the returned map are the field names, the values are
	 * Readers containing the content
	 * @param stream the input stream to filter
	 * @return the extracted data
	 */
	Map<String, Reader> doFilter(Source source) throws FilterException;
}
