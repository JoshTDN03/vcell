/*
 * Copyright (C) 1999-2011 University of Connecticut Health Center
 *
 * Licensed under the MIT License (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 *  http://www.opensource.org/licenses/mit-license.php
 */

package cbit.vcell.messaging;
import javax.jms.*;

/**
 * Insert the type's description here.
 * Creation date: (7/24/2003 11:35:47 AM)
 * @author: Fei Gao
 */
public class JmsConnectionFactoryImpl implements JmsConnectionFactory {
	private JmsProvider jmsProvider = null;

public JmsConnectionFactoryImpl() throws JMSException {
	super();
	jmsProvider = JmsProviderFactory.getJmsProvider();
}

public JmsConnectionFactoryImpl(JmsProvider jmsProvider0) {
	super();
	jmsProvider = jmsProvider0;
}

public JmsConnectionFactoryImpl(String provider, String url, String userid, String password) throws JMSException {
	super();
	jmsProvider = JmsProviderFactory.getJmsProvider(provider, url, userid, password);
}

public JmsConnection createConnection() throws JMSException {
	return new JmsConnectionImpl(jmsProvider);
}

public JmsXAConnection createXAConnection() throws JMSException {
	return new JmsXAConnectionImp(jmsProvider);
}
}
