package cbit.vcell.message.jms.activeMQ.monitor;
/* From:
 Instant Apache ActiveMQ Messaging Application Development How-to
Timothy Bish
Published 2013-05-23
 */

import java.io.PrintWriter;

import javax.jms.Connection;
import javax.jms.Queue;
import javax.jms.Session;

import org.apache.activemq.ActiveMQConnectionFactory;

public class SiteMonitor  {

    private static final String QNAMES[] = { "dbReq","simDataReq","simJob","simReq","workerEvent"};

    private final String connectionUri;  
    private ActiveMQConnectionFactory connectionFactory;
    private Connection connection;
    private Session session;
    private final String site; 
    private final PrintWriter writer;
    

    public SiteMonitor(String site, PrintWriter writer, String url) {
		super();
		this.site = site;
		this.writer = writer;
	    connectionUri = url; 
	}
    
    public void start( ) throws Exception {
        connectionFactory = new ActiveMQConnectionFactory(connectionUri);
        connection = connectionFactory.createConnection();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        for (String qn : QNAMES) {
	        Queue monitored = session.createQueue(qn + site);
	        QueueWatcher qw = new QueueWatcher(session, monitored, writer);
	        qw.start();
        }
        connection.start();
    }
    
    public void stop( ) throws Exception {
    	writer.flush();
        if (connection != null) {
            connection.close();
        }
    }
    
//    public static void main(String[] args) {
//    	Logging.init();
//    	PrintWriter writer = new  PrintWriter(new OutputStreamWriter(System.out),true);
//    	SiteMonitor t4 = new SiteMonitor("Test4", writer, "failover:(tcp://code2.cam.uchc.edu:61616)");
//    	try {
//			t4.start( );
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//    }

   
}