import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;

import javaQuery.importClass.javaQueryBundle;
import javaQuery.j2ee.GeoLocation;
import org.json.simple.*;

public class DummyServiceBorker implements MqttCallback {
	
Object waiter = new Object();
	
	/**
	 * The main entry point of the sample.
	 * 
	 * This method handles parsing the arguments specified on the
	 * command-line before performing the specified action.
	 * @throws InterruptedException 
	 */
	public static void main(String[] args) throws InterruptedException {
		
		// Default settings:
		boolean quietMode = false;
		String topic = "/iLab/Data/Mail/Raw";
		String message = "Message from MQTTv3 Java client";
		int qos = 0;
		String url = "tcp://winter.ceit.uq.edu.au:1883";//"tcp://"+broker+":"+port;
		String clientId = "ilab_Pub";
		
		Date dt=new Date();
	    SimpleDateFormat matter1=new SimpleDateFormat("dd MM");
		String day = matter1.format(dt);
		//String MIT = "18.72.0.3";
		//String UQ = "130.102.128.123";
		JSONObject UQ=new JSONObject();
		UQ.put("ServiceBroker","UQ OpeniLabs");
		UQ.put("IPaddress","130.102.128.123");
		UQ.put("Experiment Id","9755");
		UQ.put("Usergroup","The university of Queensland");
		UQ.put("Unit Id","1");
		UQ.put("StatusCode","Completed");
		UQ.put("LabServer", "Radioactivity");
		UQ.put("Date", day);
		UQ.put("Time", "04:52:48.0318+1000");
		
		JSONObject MIT=new JSONObject();
		MIT.put("ServiceBroker","Massachusetts");
		MIT.put("IPaddress","18.72.0.3");
		MIT.put("Experiment Id","9756");
		MIT.put("Usergroup","MIT");
		MIT.put("Unit Id","0");
		MIT.put("StatusCode","Completed");
		MIT.put("LabServer", "Radioactivity");
		MIT.put("Date", day);
		MIT.put("Time", "01:53:48.0318+1000");
		
		JSONObject NW=new JSONObject();
		NW.put("ServiceBroker","NorthWestern");
		NW.put("IPaddress","129.105.99.99");
		NW.put("Experiment Id","9757");
		NW.put("Usergroup","Florida Virtual School");
		NW.put("Unit Id","0");
		NW.put("StatusCode","Failed");
		NW.put("LabServer", "Radioactivity");
		NW.put("Date", day);
		NW.put("Time", "01:54:48.0318+1000");
		
		ArrayList<String> a = new ArrayList<String>();
		a.add(UQ.toString());
		//a.add(MIT.toString());
		//a.add(NW.toString());
		// With a valid set of arguments, the real work of 
		// driving the client API can begin
		
		try {
			// Create an instance of the Sample client wrapper
			DummyServiceBorker sampleClient = new DummyServiceBorker(url,clientId,quietMode);
	

			for(int i =0;i<a.size();i++)
			{
				sampleClient.publish(topic,qos,a.get(i).getBytes());
				Thread.sleep(10000);
			}
			

			
		} catch(MqttException me) {
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("casue "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
	}
    
	// Private instance variables
	private MqttClient client;
	private String brokerUrl;
	private boolean quietMode;
	private MqttConnectOptions conOpt;
	
	public void disconnect() throws MqttException
	{
		client.disconnect();
		log("Disconnected");
	}
	
	
	/**
	 * Constructs an instance of the sample client wrapper
	 * @param brokerUrl the url to connect to
	 * @param clientId the client id to connect with
	 * @param quietMode whether debug should be printed to standard out
	 * @throws MqttException
	 */
    public DummyServiceBorker(String brokerUrl, String clientId, boolean quietMode) throws MqttException {
    	this.brokerUrl = brokerUrl;
    	this.quietMode = quietMode;
    	
    	//This sample stores files in a temporary directory...
    	//..a real application ought to store them somewhere 
    	//where they are not likely to get deleted or tampered with
    	String tmpDir = System.getProperty("java.io.tmpdir");
    	MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir); 
    	
    	try {
    		// Construct the object that contains connection parameters 
    		// such as cleansession and LWAT
	    	conOpt = new MqttConnectOptions();
	    	conOpt.setCleanSession(false);

    		// Construct the MqttClient instance
			client = new MqttClient(this.brokerUrl,clientId, dataStore);
			
			// Set this wrapper as the callback handler
	    	client.setCallback(this);
	    	
		} catch (MqttException e) {
			e.printStackTrace();
			log("Unable to set up client: "+e.toString());
			System.exit(1);
		}
    }

    /**
     * Performs a single publish
     * @param topicName the topic to publish to
     * @param qos the qos to publish at
     * @param payload the payload of the message to publish 
     * @throws MqttException
     */
    public void publish(String topicName, int qos, byte[] payload) throws MqttException {
    	
    	// Connect to the server
    	client.connect();
    	log("Connected to "+brokerUrl + " with client ID "+client.getClientId());
    	
    	// Get an instance of the topic
    	MqttTopic topic = client.getTopic(topicName);

   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos);
	
    	// Publish the message
    	String time = new Timestamp(System.currentTimeMillis()).toString();
    	log("Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);
    	MqttDeliveryToken token = topic.publish(message);
	
    	// Wait until the message has been delivered to the server
    	token.waitForCompletion();
    	
    	// Disconnect the client
    	client.disconnect();
    	log("Disconnected");
    }
    


    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * @param message the message to log
     */
    private void log(String message) {
    	if (!quietMode) {
    		System.out.println(message);
    	}
    }


	
	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/
    
    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
	public void connectionLost(Throwable cause) {
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point.
		// This sample simply exits.
		log("Connection to " + brokerUrl + " lost!" + cause);
		System.exit(1);
	}

    /**
     * @see MqttCallback#deliveryComplete(MqttDeliveryToken)
     */
	public void deliveryComplete(MqttDeliveryToken token) {
		// Called when a message has completed delivery to the
		// server. The token passed in here is the same one
		// that was returned in the original call to publish.
		// This allows applications to perform asychronous 
		// delivery without blocking until delivery completes.
		
		// This sample demonstrates synchronous delivery, by
		// using the token.waitForCompletion() call in the main thread.
	}

    /**
     * @see MqttCallback#messageArrived(MqttTopic, MqttMessage)
     */
	public void messageArrived(MqttTopic topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server.
		
		String time = new Timestamp(System.currentTimeMillis()).toString();
		
		System.out.println("Time:\t" +time +
                           "  Topic:\t" + topic.getName() + 
                           "  Message:\t" + new String(message.getPayload()) +
                           "  QoS:\t" + message.getQos());
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

	
}
