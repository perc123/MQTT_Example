import org.eclipse.paho.client.mqttv3.*;

import java.io.DataInputStream;
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
                        // Publish the request message
                        System.out.println("Publishing message: " + new String(requestPayload, StandardCharsets.UTF_8));
                        client.publish(requestTopic, new MqttMessage(requestPayload));
                        topic.equals(moduleTopic);
                            processSocketData();

                    }
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                    // Delivery complete callback (not used in this example)
                }
            });
            processSocketData();
            // Publish the request message
            client.publish(requestTopic, new MqttMessage(requestPayload));

        } catch (MqttException me) {
            System.out.println("Reason: " + me.getReasonCode());
            System.out.println("Message: " + me.getMessage());
            System.out.println("Localized Message: " + me.getLocalizedMessage());
            System.out.println("Cause: " + me.getCause());
            System.out.println("Exception: " + me);
            me.printStackTrace();
        }

    }
    private static void processSocketData() {

        // Establish socket connection to receive data
        try (Socket socket = new Socket("localhost", 12000);
             InputStream inputStream = socket.getInputStream()) {

            System.out.println("Connected to the socket on port 12000");


            // Wrap the input stream in a DataInputStream to read Java primitive data types
            DataInputStream dataInputStream = new DataInputStream(inputStream);


                // Read the integer in chunks of four bytes until you have read the complete integer
                int intValue = 0;
                for (int i = 0; i < 4; i++) {
                    intValue = (intValue << 8) | (dataInputStream.read() & 0xFF);
                }
                System.out.println("Read integer value: " + intValue);


            // Read data from the socket
            System.out.println("Data: " + dataInputStream.readInt());
            // Read the first two bytes to determine the length of the data
            byte[] lengthBytes = new byte[2];
            inputStream.read(lengthBytes);
            int length = ((lengthBytes[0] & 0xFF) << 8) | (lengthBytes[1] & 0xFF);
            System.out.println(length);

            // Read the rest of the data
            byte[] dataBytes = new byte[length];
            int totalBytesRead = 0;
            while (totalBytesRead < length) {
                int bytesRead = inputStream.read(dataBytes, totalBytesRead, length - totalBytesRead);
                if (bytesRead == -1) {
                    // Handle end of stream or incomplete data
                    break;
                }
                totalBytesRead += bytesRead;
            }

            // Decrypt data using XOR cipher with key 0xbe
            byte[] decryptedData = new byte[length];
            for (int i = 0; i < length; i++) {
                decryptedData[i] = (byte) (dataBytes[i] ^ 0xb1);
            }

            // Convert decrypted data to ASCII string
            String decryptedString = new String(decryptedData, StandardCharsets.US_ASCII);
            System.out.println("Decrypted String: " + decryptedString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
