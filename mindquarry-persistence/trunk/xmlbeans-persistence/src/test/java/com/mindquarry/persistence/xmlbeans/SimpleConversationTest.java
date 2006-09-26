package com.mindquarry.persistence.xmlbeans;

import org.apache.avalon.framework.service.ServiceException;

import com.mindquarry.common.persistence.Session;
import com.mindquarry.common.persistence.SessionFactory;
import com.mindquarry.types.conversation.Conversation;

public class SimpleConversationTest extends XmlBeansPersistenceTestBase {

    public void testConversation() throws ServiceException {
        SessionFactory sessionFactory = (SessionFactory) lookup(SessionFactory.ROLE);
        Session session = sessionFactory.currentSession();
        
        Conversation conv = (Conversation) session.newEntity(Conversation.class);
        
        String greatUuid = "00,1145646412313df14534s";
        conv.setId(greatUuid);        
        conv.setName("Conversation Title");        
        
        session.persist(conv);
        
        
        //conv = (Conversation) persistence.query("GetById", new Object[] {greatUuid});
    }
}
