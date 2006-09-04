/*
 * Copyright (C) 2006, Mindquarry GmbH 
 */
package com.mindquarry.conversation.mailets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.jcr.Node;
import javax.jcr.NodeIterator;
import javax.jcr.PathNotFoundException;
import javax.jcr.RepositoryException;
import javax.mail.MessagingException;

import org.apache.mailet.Mail;
import org.apache.mailet.MailAddress;
import org.apache.mailet.MailetException;

/**
 * @author <a hef="mailto:alexander(dot)saar(at)mindquarry(dot)com</a>
 */
public class ConversationManagementMailet extends AbstractConversationMailet {
	/**
	 * Initialize the conversation mailet.
	 * 
	 * @throws MailetException
	 *             Thrown if a required parameter is missing.
	 */
	@Override
	public void init() throws MessagingException {
		super.init();
	}

	/**
	 * @see org.apache.mailet.GenericMailet#service(org.apache.mailet.Mail)
	 */
	@Override
	public void service(Mail mail) throws MessagingException {
		System.out.println("Start processing of received mail...");

		try {
			// check if the specified project(s) exist
			List<String> projects = getProjects(mail);
			if (projects.size() == 0) {
				mail.setState(Mail.GHOST);
				return;
			}
			// process the mail for each project
			for (String project : projects) {
				processMail(project, mail);
			}
			attachFooter(mail);
		} catch (Exception e) {
			throw (new MessagingException());
		}
	}

	private List<String> getProjects(Mail mail) throws RepositoryException,
			MessagingException {
		Collection recipients = mail.getRecipients();
		List<String> projectNames = new ArrayList<String>();
		for (Object object : recipients) {
			MailAddress recipient = (MailAddress) object;
			String recipientName = recipient.getUser();

			log("Checking recipient " + recipientName + "...");
			String projectName = recipientName.split("-")[0];

			// TODO use query manager instead of direct access for searching
			// projects

			// QueryManager qm = session.getWorkspace().getQueryManager();
			// Query query = qm.createQuery("", Query.XPATH);
			// QueryResult result = query.execute();

			// check if the given project exists
			boolean found = false;
			Node projectsNode = getSession().getRootNode().getNode("projects");
			NodeIterator nit = projectsNode.getNodes();
			while (nit.hasNext()) {
				Node projectNode = (Node) nit.next();
				if (projectNode.getProperty("name").equals(projectName)) {
					projectNames.add(projectNode.getName());
					break;
				}
			}
			// if a project was not found, inform the sender
			if (!found) {
				getMailetContext().bounce(mail,
						"Project '" + projectName + "' does not exist.");
			}
		}
		return (projectNames);
	}

	private void processMail(String projectName, Mail mail)
			throws PathNotFoundException, RepositoryException,
			MessagingException {
		Collection allRecipients = mail.getRecipients();

		// get recipients for the given project
		List<String> recipients = new ArrayList<String>();
		for (Object object : allRecipients) {
			MailAddress recipient = (MailAddress) object;
			String recipientName = recipient.getUser();
			String tmpProjectName = recipientName.split("-")[0];

			if (tmpProjectName.equals(projectName)) {
				recipients.add(recipientName);
			}
		}
		// processing project tags
		List<Node> tags = new ArrayList<Node>();
		List<String> newTags = new ArrayList<String>();

		Node tagsNode = getSession().getRootNode().getNode(
				"projects/" + projectName + "/tags");

		for (String recipient : recipients) {
			String tagName = recipient.split("-")[1];

			// TODO use query manager for finding tags

			// check if the tag already exist
			boolean found = false;
			NodeIterator nit = tagsNode.getNodes();
			while (nit.hasNext()) {
				Node tagNode = (Node) nit.next();
				if (tagNode.getProperty("name").equals(tagName)) {
					found = true;
					tags.add(tagNode);
					break;
				}
			}
			// remember tags that can't be found for later processing
			if (!found) {
				newTags.add(tagName);
			}
		}
		// check if there are tags that already exist, if not inform the sender
		// that nobody has received the mail
		if (tags.isEmpty()) {
			getMailetContext().bounce(mail,
					"Nobody has subscribed to the tags of your mail.");
		}
		// process new tags
		for (String tagName : newTags) {
			Node tagNode = tagsNode.addNode("tag");
			tagNode.setProperty("name", tagName);
			tagNode.addMixin("mix:referenceable");
			tags.add(tagNode);
		}
		// TODO get subscriber
		
		// TODO send mail
	}
}
