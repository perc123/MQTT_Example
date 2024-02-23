import com.hivemq.client.mqtt.MqttClient;
import com.hivemq.client.mqtt.mqtt3.Mqtt3AsyncClient;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class App {
    public static void main(String[] args) throws InterruptedException {
        String clientId = "fanis-client";
        String moduleTopic = "IotTestModule";
        String requestTopic = "IotTestModuleRequest";
        byte[] requestPayload = "0123456789".getBytes(StandardCharsets.UTF_8);
        AtomicBoolean isConnected = new AtomicBoolean(true);

        // Block the main thread until messages are processed
        CountDownLatch latch = new CountDownLatch(1);
        CountDownLatch processLatch = new CountDownLatch(1); // Latch for the ProcessSocketData to end App

        // Create an MQTT client
        Mqtt3AsyncClient client = MqttClient.builder()
                .serverHost("localhost")
                .serverPort(1883)
                .identifier(clientId)
                .useMqttVersion3().buildAsync();

        client.connectWith().send();


        client.subscribeWith()
                .topicFilter(moduleTopic)
                .callback(context -> {
                    System.out.println("socket");
                    String receivedMessage = new String(context.getPayloadAsBytes(), StandardCharsets.UTF_8);
                    String xorKey = ProcessSocketData.extractXorKey(receivedMessage);
                    if (xorKey != null) {
                        int xorKeyValue = Integer.parseInt(xorKey, 16);
                        ProcessSocketData.processSocketData(xorKeyValue);
                        processLatch.countDown();
                        isConnected.set(false);
                    }
                })
                .send();


        while(isConnected.get()){
            client.publishWith()
                    .topic(requestTopic)
                    .payload(requestPayload)
                    .send();
        }

        if (!isConnected.get())
            client.disconnect();

        // Wait for messages to be processed
        latch.await(1, TimeUnit.SECONDS);

    }
}

