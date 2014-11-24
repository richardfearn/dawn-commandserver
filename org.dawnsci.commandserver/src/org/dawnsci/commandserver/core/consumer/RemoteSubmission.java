/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.commandserver.core.consumer;

import java.net.URI;
import java.util.Collection;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.TreeSet;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Message;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.dawnsci.commandserver.core.ConnectionFactoryFacade;
import org.dawnsci.commandserver.core.beans.StatusBean;

import com.fasterxml.jackson.databind.ObjectMapper;

public class RemoteSubmission {
	
	private URI    uri;
	private String uniqueId;
	private String queueName;
	private int    priority;
	private long   lifeTime;
	private long   timestamp;
	
	public RemoteSubmission(URI uri) {
	    this.uri       = uri;
	    this.uniqueId = System.currentTimeMillis()+"_"+UUID.randomUUID();
	}

	/**
	 * Submits the bean onto the server. From there events about this
	 * bean are tacked by monitoring the status queue.
	 * 
	 * @param uri
	 * @param bean
	 */
	public synchronized TextMessage submit(StatusBean bean, boolean prepareBean) throws Exception {

		
		if (getQueueName()==null || "".equals(getQueueName())) throw new Exception("Please specify a queue name!");
		
		
		Connection      send     = null;
		Session         session  = null;
		MessageProducer producer = null;
		
		try {
			QueueConnectionFactory connectionFactory = ConnectionFactoryFacade.createConnectionFactory(uri);
			send              = connectionFactory.createConnection();

			session = send.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Queue queue = session.createQueue(queueName);

			producer = session.createProducer(queue);
			producer.setDeliveryMode(DeliveryMode.PERSISTENT);

			ObjectMapper mapper = new ObjectMapper();

			if (getTimestamp()<1) setTimestamp(System.currentTimeMillis());
			if (getPriority()<1)  setPriority(1);
			if (getLifeTime()<1)  setLifeTime(7*24*60*60*1000); // 7 days in ms

			if (prepareBean) {
				if (bean.getUserName()==null)   bean.setUserName(System.getProperty("user.name"));
				bean.setUniqueId(uniqueId);
				bean.setSubmissionTime(getTimestamp());
			}
			String   jsonString = mapper.writeValueAsString(bean);
			
			TextMessage message = session.createTextMessage(jsonString);
			

			message.setJMSMessageID(uniqueId);
			message.setJMSExpiration(getLifeTime());
			message.setJMSTimestamp(getTimestamp());
			message.setJMSPriority(getPriority());
			
			producer.send(message);
			
			return message;
			
		} finally {
			if (send!=null)     send.close();
			if (session!=null)  session.close();
			if (producer!=null) producer.close();
		}

	}
	
	/**
	 * Monitors a given bean in the status queue. 
	 * If the bean is not there throws exception.
	 * If the bean is in a final state, returns the bean straight away.
	 * 
	 * Polls the queue for the unique id of the bean we want until it
	 * encounters a final state of that bean.
	 * 
	 * Polling rate is less than 1s
	 * 
	 * NOTE this class can poll forever if the job it is looking at never finishes.
	 * 
	 * @param obean
	 * @param string
	 * @return the bean once it is in a final state.
	 * @throws exception if broker or queue absent
	 */
	public StatusBean monitor(StatusBean obean) throws Exception {
		
		if (getQueueName()==null || "".equals(getQueueName())) throw new Exception("Please specify a queue name!");
		
		QueueConnectionFactory connectionFactory = ConnectionFactoryFacade.createConnectionFactory(uri);
		QueueConnection qCon  = connectionFactory.createQueueConnection(); // This times out when the server is not there.
		QueueSession    qSes  = qCon.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
		Queue queue   = qSes.createQueue(queueName);
		qCon.start();
		
	    QueueBrowser qb = qSes.createBrowser(queue);
   
    	Class        clazz  = obean.getClass();
    	ObjectMapper mapper = new ObjectMapper();
	   
    	try {
	    	POLL: while(true) {
	    		
	    		Thread.sleep(500);
	    		
		    	@SuppressWarnings("rawtypes")
		    	Enumeration  e  = qb.getEnumeration();
	
		    	while(e.hasMoreElements()) { // We must final the bean somewhere.
		    		Message m = (Message)e.nextElement();
		    		if (m==null) continue;
		    		if (m instanceof TextMessage) {
		    			TextMessage t = (TextMessage)m;
		    			final StatusBean bean = mapper.readValue(t.getText(), clazz);
		    			if (bean.getUniqueId().equals(obean.getUniqueId())) {
		    				if (bean.getStatus().isFinal()) return bean;
System.out.println(bean.getPercentComplete());
		    				continue POLL;
		    			}
		    		}
		    	}
		    	
		    	throw new Exception("The bean with id "+obean.getUniqueId()+" does not exist in "+getQueueName()+"!");
	
		    }
    	} finally {
    		qCon.close();
    	}
	}


	public String getQueueName() {
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public long getLifeTime() {
		return lifeTime;
	}

	public void setLifeTime(long lifeTime) {
		this.lifeTime = lifeTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (lifeTime ^ (lifeTime >>> 32));
		result = prime * result
				+ ((uniqueId == null) ? 0 : uniqueId.hashCode());
		result = prime * result + priority;
		result = prime * result
				+ ((queueName == null) ? 0 : queueName.hashCode());
		result = prime * result + (int) (timestamp ^ (timestamp >>> 32));
		result = prime * result + ((uri == null) ? 0 : uri.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		RemoteSubmission other = (RemoteSubmission) obj;
		if (lifeTime != other.lifeTime)
			return false;
		if (uniqueId == null) {
			if (other.uniqueId != null)
				return false;
		} else if (!uniqueId.equals(other.uniqueId))
			return false;
		if (priority != other.priority)
			return false;
		if (queueName == null) {
			if (other.queueName != null)
				return false;
		} else if (!queueName.equals(other.queueName))
			return false;
		if (timestamp != other.timestamp)
			return false;
		if (uri == null) {
			if (other.uri != null)
				return false;
		} else if (!uri.equals(other.uri))
			return false;
		return true;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}
	
}
