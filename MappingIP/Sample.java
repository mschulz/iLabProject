/* 
 * Copyright (c) 2009, 2012 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Dave Locke - initial API and implementation and/or initial documentation
 */



import java.io.IOException;
import java.sql.Timestamp;

import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttDefaultFilePersistence;
import org.eclipse.paho.client.mqttv3.MqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import javaQuery.importClass.javaQueryBundle;
import javaQuery.j2ee.GeoLocation;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

/**
 * This sample application demonstrates basic usage
 * of the MQTT v3 Client api.
 * 
 * It can be run in one of two modes:
 *  - as a publisher, sending a single message to a topic on the server
 *  - as a subscriber, listening for messages from the server
 *
 */
public class Sample implements MqttCallback {
	Object waiter = new Object();
	
	/**
	 * The main entry point of the sample.
	 * 
	 * This method handles parsing the arguments specified on the
	 * command-line before performing the specified action.
	 */
	public static void main(String[] args) {
        
		// Default settings:
		boolean quietMode = false;
		//String action = "publish";
		String action = "subscribe";
		String topic = ""; //data is in JSON format
		String message = "12345";//obj.toString();
		int qos = 0;
	
		if (topic.equals("")) {
			// Set the default topic according to the specified action
			if (action.equals("publish")) {
				//topic = "ilab/radioation-1/IP";
				topic = "test";
			} else {
				topic = Sub_topic;
			}
		}
		
		//String url = "tcp://winter.ceit.uq.edu.au:1883";//"tcp://"+broker+":"+port;
		String url = "tcp://winter.ceit.uq.edu.au:1883";//"tcp://"+broker+":"+port;
		String clientId = "ilab_"+action;

		// With a valid set of arguments, the real work of 
		// driving the client API can begin
		
		try {
			// Create an instance of the Sample client wrapper
			Sample sampleClient = new Sample(url,clientId,quietMode);
			
			// Perform the specified action
			if (action.equals("publish")) {
				sampleClient.publish(topic,qos,message.getBytes());
			} else if (action.equals("subscribe")) {
				sampleClient.subscribe(topic,qos);
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
	private String Pub_topic = "test";
	private static String Sub_topic = "/iLab/Data/Mail/Raw";
	
	/**
	 * Constructs an instance of the sample client wrapper
	 * @param brokerUrl the url to connect to
	 * @param clientId the client id to connect with
	 * @param quietMode whether debug should be printed to standard out
	 * @throws MqttException
	 */
    public Sample(String brokerUrl, String clientId, boolean quietMode) throws MqttException {
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
    	//client.connect();
    	//log("Connected to "+brokerUrl + " with client ID "+client.getClientId());
    	
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
    	//client.disconnect();
    	//log("Disconnected");
    }
    
    /**
     * Subscribes to a topic and blocks until Enter is pressed
     * @param topicName the topic to subscribe to
     * @param qos the qos to subscibe at
     * @throws MqttException
     */
    public void subscribe(String topicName, int qos) throws MqttException {
    	
    	// Connect to the server
    	client.connect();
    	log("Connected to "+brokerUrl+" with client ID "+client.getClientId());

    	// Subscribe to the topic
    	log("Subscribing to topic \""+topicName+"\" qos "+qos);
    	client.subscribe(topicName, qos);

    	// Block until Enter is pressed
    	log("Press <Enter> to exit");
		try {
			System.in.read();
		} catch (IOException e) {
			//If we can't read we'll just exit
		}
		
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
		//System.exit(1);
		log("Reconnection");
		try {
			this.subscribe(Sub_topic,0);
		} catch (MqttSecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
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
		
		//subscribe the ip address from message broker and map it to latitude and longtitude
		Object temp=JSONValue.parse(message.toString());
		JSONObject test= (JSONObject)temp;
		
		String LabServer = test.get("LabServer").toString();
		if(LabServer.equals("Radioactivity"))
		{
			String IP = test.get("IPaddress").toString();
			String Usergroup = test.get("Usergroup").toString();
			String ServiceBroker = test.get("ServiceBroker").toString();
			String ExperimentID = test.get("Experiment Id").toString();
			String UnitID = test.get("Unit Id").toString();
			String Status = test.get("StatusCode").toString();
			String Date = test.get("Date").toString();
			String Time = test.get("Time").toString();
			
			
			Double Lat =null;
			Double Long =null;
			if(IP.equals("130.102.128.123"))
			{
				Long = 153.013116;
				Lat = -27.49835;
			}
			else if(IP.equals("129.105.99.99"))
			{
				Long = -87.674497;
				Lat =  42.056025;
			}
			else if(IP.equals("18.72.0.3"))
			{
				Long =  -71.092043;
				Lat =   42.361073;
			}
			
			else{
			//based on ip and get the latitude and longtitude
			GeoLocation $gl = javaQueryBundle.createGeoLocation();
	        $gl.MAPTargetByIP(IP, "This is Demo. You can set even NULL");
	        Lat = Double.parseDouble($gl.Latitude);
	        Long = Double.parseDouble($gl.Longitude);
			
			}
			
			
			System.out.println(IP+" has been mapped to: "+Lat+", "+Long);
			
			String[]  time= Time.split(":");
			String hour = time[0];
			String min = time[1];
			String add = time[2].replace('+', ':');
			String[] Add = add.split(":");
			String addh = Add[1];
			String b = addh.substring(0,2);
			int h = Integer.parseInt(b);
			int h1 =Integer.parseInt(hour);
			int newH = h+h1;
			
			String Time1 = newH+":"+min;		
			
			
			JSONObject obj=new JSONObject();
			obj.put("Usergroup",Usergroup);
			obj.put("ServiceBroker",ServiceBroker);
			obj.put("Experiment Id",ExperimentID);
			obj.put("Unit Id",UnitID);
			obj.put("Lat",Lat);
			obj.put("Long",Long);
			obj.put("StatusCode",Status);
			obj.put("Date", Date);
			obj.put("Time", Time1);
			
			this.publish(Pub_topic, 0, obj.toString().getBytes());
			
		}
		
		
		
		
		
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

	



}