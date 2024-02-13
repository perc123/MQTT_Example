import org.eclipse.paho.client.mqttv3.*;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class App {
    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "my-mqtt-client-id";
        String moduleTopic = "IotTestModule";
        String requestTopic = "IotTestModuleRequest";
        byte[] requestPayload = "0123456789".getBytes(StandardCharsets.UTF_8);

        try {
            ProcessSocketData processSocketData = new ProcessSocketData();
            MqttCallback mqttCallback = new MqttCallback();
            // Connect to MQTT Broker
            MqttClient client = new MqttClient(broker, clientId);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);

            System.out.println("Connecting to broker: " + broker);
            client.connect(connOpts);
            System.out.println("Connected");

            // Subscribe to the module topic
            System.out.println("Subscribing to topic \"" + moduleTopic + "\"");
            client.subscribe(moduleTopic);

            // Set up callback for MQTT messages
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable throwable) {
                    System.out.println("Connection to MQTT broker lost!");
                }

                @Override
                public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                    // Handle incoming MQTT message
                    if (topic.equals(moduleTopic)) {
                        System.out.println("Received message from module: " + new String(mqttMessage.getPayload()));
                        String receivedMessage = new String(mqttMessage.getPayload(), StandardCharsets.UTF_8);
                        String xorKey = ProcessSocketData.extractXorKey(receivedMessage);
                        if (xorKey != null) {
                            int xorKeyValue = Integer.parseInt(xorKey, 16);
                            ProcessSocketData.processSocketData(xorKeyValue);
                        }
                    }
                    if (topic.equals(requestTopic)){
                        client.publish(requestTopic, new MqttMessage(requestPayload));
                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    // Delivery complete callback (not used in this example)
                }

            });

        } catch (MqttException me) {
            System.out.println("Message: " + me.getMessage());
            System.out.println("Localized Message: " + me.getLocalizedMessage());
            System.out.println("Cause: " + me.getCause());
            System.out.println("Exception: " + me);
            me.printStackTrace();
        }

    }
}
