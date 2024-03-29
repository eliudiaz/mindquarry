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
package com.mindquarry.common.index;

import java.util.List;

import org.apache.avalon.framework.logger.AbstractLogEnabled;

import com.mindquarry.common.index.events.DocumentIndexedEvent;
import com.mindquarry.common.index.events.DocumentRemovedFromIndexEvent;
import com.mindquarry.events.EventBroker;
import com.mindquarry.events.exception.EventAlreadyRegisteredException;
import com.mindquarry.events.exception.UnknownEventException;

/**
 * Add summary documentation here.
 * 
 * @author <a href="mailto:alexander(dot)saar(at)mindquarry(dot)com">Alexander
 *         Saar</a>
 */
public abstract class AbstractAsyncIndexClient extends AbstractLogEnabled
        implements IndexClient {
    protected EventBroker broker;

    /**
     * Starts a new thread for asynchronous indexing. The thread calls the
     * {@link indexInternal()} function which must be overridden by child
     * classes. Within this class the indexing functionality is implemented.
     * 
     * @see com.mindquarry.common.index.Indexer#index(List<String>, List<String>)
     */
    public void index(final List<String> changedPaths,
            final List<String> deletedPaths) {
        Thread thread = new Thread(new Runnable() {
            /**
             * Calls the internal indexing function.
             * 
             * @see java.lang.Runnable#run()
             */
            public void run() {
                try {
                    throwDocumentIndexedEvent(changedPaths, deletedPaths);
                    indexInternal(changedPaths, deletedPaths);
                } catch (Exception e) {
                    getLogger()
                            .error(
                                    "An error occured while indexing client processes the list of changed paths.",
                                    e);
                }
            }
        });
        thread.start();
    }

    /**
     * Abstract index function to be overridden by child classes of this
     * abstract indexer. Child classes shall implement indexing functionality
     * within this function.
     */
    protected abstract void indexInternal(List<String> changedPaths,
            List<String> deletedPaths) throws Exception;

    /**
     * Calls indexInternal() without starting a new thread, thus indexing is
     * done asynchronously. The {@link indexInternal()} function must be
     * overridden by child classes. Within this class the indexing functionality
     * is implemented.
     * 
     * @see com.mindquarry.common.index.Indexer#index(List<String>, List<String>)
     */
    public void indexSynch(final List<String> changedPaths,
            final List<String> deletedPaths) {
        try {
            throwDocumentIndexedEvent(changedPaths, deletedPaths);
            indexInternal(changedPaths, deletedPaths);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void throwDocumentIndexedEvent(final List<String> changedPaths,
            final List<String> deletedPaths) {
        for (String item : changedPaths) {
            try {
                broker
                        .publishEvent(new DocumentIndexedEvent(this, item),
                                false);
            } catch (UnknownEventException e) {
                // event was not registered previously, so we can skip event
                // throwing
                break;
            }
        }
        for (String item : deletedPaths) {
            try {
                broker.publishEvent(new DocumentRemovedFromIndexEvent(this,
                        item), false);
            } catch (UnknownEventException e) {
                // event was not registered previously, so we can skip event
                // throwing
                break;
            }
        }
    }

    /**
     * Setter for the event broker used by this MBean.
     * 
     * @param broker the broker to set
     */
    public void setBroker(EventBroker broker) {
        this.broker = broker;
    }

    /**
     * Used to initialize the HTTP client.
     */
    public void initialize() throws Exception {
        try {
            broker.registerEvent(DocumentIndexedEvent.ID);
        } catch (EventAlreadyRegisteredException e) {
            // event was already registered, nothing to to here
        }
        try {
            broker.registerEvent(DocumentRemovedFromIndexEvent.ID);
        } catch (EventAlreadyRegisteredException e) {
            // event was already registered, nothing to to here
        }
    }
}
