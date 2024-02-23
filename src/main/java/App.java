import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) {
        String broker = "tcp://localhost:1883";
        String clientId = "fanis-client";
        String moduleTopic = "IotTestModule";
        String requestTopic = "IotTestModuleRequest";
        byte[] requestPayload = "0123456789".getBytes(StandardCharsets.UTF_8);

        // Create a latch to block the main thread until messages are processed
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch processLatch = new CountDownLatch(1);

        try {
            // Create an MQTT client
            Mqtt3AsyncClient client = MqttClient.builder()
                    .serverHost("localhost")
                    .serverPort(1883)
                    .identifier(clientId)
                    .useMqttVersion3().buildAsync();


            // Connect to the MQTT broker
            client.connectWith()
                    .send()
                    .whenComplete((connAck, throwable) -> {
                                if (throwable != null) {
                                    System.out.println("Not connected");
                                } else {
                                    client.subscribeWith()
                                            .topicFilter(moduleTopic)
                                            .callback(context -> {
                                                System.out.println("connected");
                                                String receivedMessage = new String(context.getPayloadAsBytes(), StandardCharsets.UTF_8);
                                                System.out.println(receivedMessage);
                                                // Publish a message to the request topic
                                                client.publishWith()
                                                        .topic(requestTopic)
                                                        .payload(requestPayload)
                                                        .send()
                                                        .whenComplete((publish, throwable1) -> {
                                                            if (throwable1 != null) {
                                                                throwable1.printStackTrace();
                                                            } else {
                                                                System.out.println("Published message to " + requestTopic);
                                                            }
                                                        });
                                                latch.countDown();
                                            })
                                            .send();
                                }
                            });

             // Subscribe to the module topic and handle incoming messages
             client.subscribeWith()
                     .topicFilter(moduleTopic)
                     .callback(context -> {
                         String receivedMessage = new String(context.getPayloadAsBytes(), StandardCharsets.UTF_8);
                         String xorKey = ProcessSocketData.extractXorKey(receivedMessage);
                         if (xorKey != null) {
                             int xorKeyValue = Integer.parseInt(xorKey, 16);
                             ProcessSocketData.processSocketData(xorKeyValue);
                             processLatch.countDown();
                         }
                     })
                     .send();
            try {
                processLatch.await(); // Wait until ProcessSocketData is completed
                client.disconnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

                 // Wait for messages to be processed
                 latch.await(5, TimeUnit.SECONDS);

        } catch (InterruptedException interruptedException) {
            interruptedException.printStackTrace();
        }
    }
}

