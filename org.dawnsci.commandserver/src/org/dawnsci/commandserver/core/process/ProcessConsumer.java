/*
 * Copyright (c) 2012 Diamond Light Source Ltd.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.dawnsci.commandserver.core.process;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.dawnsci.commandserver.core.ActiveMQServiceHolder;
import org.dawnsci.commandserver.core.application.IConsumerExtension;
import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.IEventService;
import org.eclipse.scanning.api.event.core.IConsumer;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IProcessCreator;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.event.status.StatusBean;

/**
 * This consumer monitors a queue and starts runs based
 * on what is submitted.
 * 
 * Please extend this consumer to create it and call the start method.
 * 
 * You must have the no argument constructor because the org.dawnsci.commandserver.core.application.Consumer
 * application requires this to start and stop the consumer.
 * 
 * @author Matthew Gerring
 *
 */
public abstract class ProcessConsumer<T extends StatusBean> implements IConsumerExtension {

	private String submitQName, statusTName, statusQName;
	protected Map<String, String> config;

	protected boolean durable = true;
	protected URI     uri;
	
	private IConsumer<T> consumer;
	protected String             consumerVersion;
	
	public ProcessConsumer() {
		this.consumerVersion = "1.0";
	}
	
	/**
	 * Method which configures the submission consumer for the queues and topics required.
	 * 
     * uri       activemq URI, e.g. tcp://sci-serv5.diamond.ac.uk:61616 
     * submit    queue to submit e.g. scisoft.xia2.SUBMISSION_QUEUE 
     * topic     topic to notify e.g. scisoft.xia2.STATUS_TOPIC 
     * status    queue for status e.g. scisoft.xia2.STATUS_QUEUE 
	 * 
	 * @param configuration
	 * @throws Exception
	 */
	public void init(Map<String, String> configuration) throws Exception {
		
		config = Collections.unmodifiableMap(configuration);
		setUri(new URI(config.get("uri")));
		this.submitQName = config.get("submit");
		this.statusTName = config.get("topic");
		this.statusQName = config.get("status");
	}

	/**
	 * Starts the consumer and does not return.
	 * @throws Exception
	 */
	public void start() throws Exception {
		
		IEventService service = ActiveMQServiceHolder.getEventService();
		this.consumer = service.createConsumer(uri, submitQName, statusQName, statusTName, IEventService.HEARTBEAT_TOPIC, IEventService.KILL_TOPIC);
		consumer.setRunner(new IProcessCreator<T>() {
			@Override
			public IConsumerProcess<T> createProcess(T bean, IPublisher<T> publisher) throws EventException {
				try {
					ProgressableProcess<T> process = ProcessConsumer.this.createProcess(bean, publisher);
					process.setArguments(config);
					return process;
				} catch (Exception ne) {
					throw new EventException("Problem creating process!", ne);
				}
			}
		});
		consumer.setName(getName());
		consumer.cleanQueue(statusQName);
		
		// This is the blocker
		consumer.run();
	}
	
	/**
	 * You may override this method to stop the consumer cleanly. Please
	 * call super.stop() if you do.
	 * @throws JMSException 
	 */
	public void stop() throws Exception {
		consumer.stop();
		consumer.disconnect();
	}

	/**
	 * 
	 * @return the name which the user will see for this consumer.
	 */
	public abstract String getName();

	/**
	 * Implement to return the actual bean class in the queue
	 * @return
	 */
	protected abstract Class<T> getBeanClass();
	
	/**
	 * Implement to create the required command server process.
	 * 
	 * @param uri
	 * @param statusTName
	 * @param statusQName
	 * @param bean
	 * @return the process or null if the message should be consumed and nothing done.
	 */
	protected abstract ProgressableProcess<T> createProcess(T bean, IPublisher<T> publisher) throws Exception;
	
	// TODO FIXME
	protected volatile int processCount;


	/**
	 * Override to stop handling certain events in the queue.
	 * @param bean
	 * @return
	 */
	protected boolean isHandled(StatusBean bean) {
		return true;
	}
		
	protected static final long TWO_DAYS = 48*60*60*1000; // ms
	protected static final long A_WEEK   = 7*24*60*60*1000; // ms

	/**
	 * Defines the time in ms that a job may be in the running state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old running jobs older than 
	 * this age.
	 * 
	 * @return
	 */
	protected long getMaximumRunningAge() {
		if (System.getProperty("org.dawnsci.commandserver.core.maximumRunningAge")!=null) {
			return Long.parseLong(System.getProperty("org.dawnsci.commandserver.core.maximumRunningAge"));
		}
		return TWO_DAYS;
	}
		
	/**
	 * Defines the time in ms that a job may be in the complete (or other final) state
	 * before the consumer might consider it for deletion. If a consumer
	 * is restarted it will normally delete old complete jobs older than 
	 * this age.
	 * 
	 * @return
	 */
	protected long getMaximumCompleteAge() {
		if (System.getProperty("org.dawnsci.commandserver.core.maximumCompleteAge")!=null) {
			return Long.parseLong(System.getProperty("org.dawnsci.commandserver.core.maximumCompleteAge"));
		}
		return A_WEEK;
	}



	public boolean isDurable() {
		return durable;
	}

	public void setDurable(boolean durable) {
		this.durable = durable;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}

	public String getConsumerVersion() {
		return consumerVersion;
	}

	public void setConsumerVersion(String consumerVersion) {
		this.consumerVersion = consumerVersion;
	}

}
