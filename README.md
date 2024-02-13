Decrypted message:

*to INSIDE M2M, datetime=2024-02-12T21:42:44.894433400, verification=YHi/ylN9l2AKxV2+SWW19enBCYIlRiRaYbioXkylHX8=*

ProcessSocketData class is processing the Data incoming from the socket,
as well as searched for the key pattern in the incoming message 
(since the key is each time newly generated) and extracts the XOR key for decryption.

**Quellen**

https://stackoverflow.com/questions/22563986/understanding-getinputstream-and-getoutputstream
https://javabeginners.de/Netzwerk/Socketverbindung.php

https://javadoc.io/doc/com.hivemq/hivemq-mqtt-client/latest/index.html
https://www.emqx.com/en/blog/connecting-to-serverless-mqtt-broker-with-paho-java
https://mosquitto.org/man/mosquitto-8.html

https://eclipse.dev/paho/index.php?page=clients/java/index.php#
https://stackoverflow.com/questions/26600192/decoding-hexadecimal-xor-encryption
https://www.baeldung.com/java-mqtt-client
https://github.com/eclipse/paho.mqtt.java/tree/master/org.eclipse.paho.client.mqttv3/src/main/java/org/eclipse/paho/client/mqttv3