package cbit.vcell.message.jms.activeMQ;

import java.net.URI;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.vcell.util.PropertyLoader;

import cbit.vcell.message.VCDestination;
import cbit.vcell.message.VCMessageSelector;
import cbit.vcell.message.VCMessagingException;
import cbit.vcell.message.VCellQueue;
import cbit.vcell.message.jms.VCMessagingServiceJms;

public class VCMessagingServiceActiveMQ extends VCMessagingServiceJms {
	private BrokerService broker = null;
	
	public VCMessagingServiceActiveMQ() {
		super();
	}
	
	@Override
	public ConnectionFactory createConnectionFactory(){
		//return new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false&broker.useJmx=false&create=false");
		String jmsUrl = PropertyLoader.getRequiredProperty(PropertyLoader.jmsURL);
		return new ActiveMQConnectionFactory(jmsUrl);
	}
	
	@Override
	protected void init(boolean bStartBroker) throws VCMessagingException {
		if (bStartBroker){
			this.broker = new BrokerService();
	
			try {
				TransportConnector connector = new TransportConnector();
				String jmsUrl = PropertyLoader.getRequiredProperty(PropertyLoader.jmsURL);
				connector.setUri(new URI(jmsUrl));
				broker.addConnector(connector);
				broker.start();
			} catch (Exception e) {
				e.printStackTrace();
				throw new VCMessagingException(e.getMessage());
			}
		}
	}

	@Override
	public MessageConsumer createConsumer(Session jmsSession, VCDestination vcDestination, VCMessageSelector vcSelector, int prefetchLimit) throws JMSException {
		Destination jmsDestination;
		MessageConsumer jmsMessageConsumer;
		if (vcDestination instanceof VCellQueue){
			jmsDestination = jmsSession.createQueue(vcDestination.getName()+"?consumer.prefetchSize="+prefetchLimit);							
		}else{
			jmsDestination = jmsSession.createTopic(vcDestination.getName()+"?consumer.prefetchSize="+prefetchLimit);							
		}
		if (vcSelector==null){
			jmsMessageConsumer = jmsSession.createConsumer(jmsDestination);
		}else{
			jmsMessageConsumer = jmsSession.createConsumer(jmsDestination,vcSelector.getSelectionString());
		}
		return jmsMessageConsumer;
	}

}
