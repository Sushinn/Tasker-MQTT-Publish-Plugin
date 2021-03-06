package net.nosybore.mqttpublishplugin;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttTopic;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public final class FireReceiver extends BroadcastReceiver {
	
	private MqttClient client;
	String mServer, mPort, mUsername, mPassword, mTopic, mPayload;
    Boolean mRetain;
    Integer nPort;
    private int mQoS;

    class SendMqttMessage extends AsyncTask<Void, Void, Void> {

	    @Override
		protected Void doInBackground(Void... v) {

            try {

                final MqttTopic messageTopic = client.getTopic(mTopic);
                final MqttMessage message = new MqttMessage(String.valueOf(mPayload).getBytes());
                final MqttConnectOptions options = new MqttConnectOptions();
                message.setRetained(mRetain);
                switch (mQoS){
                    case R.id.rbQoS0:
                    default:
                        message.setQos(0);
                        break;
                    case R.id.rbQoS1:
                        message.setQos(1);
                        break;
                    case R.id.rbQoS2:
                        message.setQos(2);
                        break;
                }
                if(mUsername != null && !mUsername.trim().equals("")) {
                    options.setUserName(mUsername);
                    options.setPassword(mPassword.toCharArray());
                }
                client.connect(options);
                messageTopic.publish(message);

                Log.d("Receiver", "Published data. Topic: " + messageTopic.getName() + " Retain Flag: " + message.isRetained() + "  Message: " + message + " QoS:" + message.getQos());

                client.disconnect();
                
                return null;

            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            }
            return null;
	    }

	    @Override
		protected void onPostExecute(Void v) { }
	}
	
    @Override
    public void onReceive(final Context context, final Intent intent) {    
    	
        	mServer = intent.getStringExtra("Server");
            nPort = Integer.parseInt(mPort = intent.getStringExtra("Port"));
        	mUsername = intent.getStringExtra("Username");
        	mPassword = intent.getStringExtra("Password");
        	mTopic = intent.getStringExtra("Topic");
        	mPayload = intent.getStringExtra("Payload");
            mRetain = intent.getBooleanExtra("Retain", false);
            mQoS = intent.getIntExtra(BundleExtraKeys.QOS, 0);
        	       	
        	final String BROKER_URL = "tcp://"+mServer+":"+nPort;
            
            try {
                client = new MqttClient(BROKER_URL, MqttClient.generateClientId(), new MemoryPersistence());
            } catch (MqttException e) {
                e.printStackTrace();
                System.exit(1);
            }
            
            new SendMqttMessage().execute();  
    }
}
