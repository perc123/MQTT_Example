import java.io.DataInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.spec.RSAOtherPrimeInfo;

public class ProcessSocketData {
    private static final String MESSAGE_PATTERN = "key=0x([0-9a-fA-F]+)";

    // Find the random generated key pattern to extract the key
    public static String extractXorKey(String message) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(MESSAGE_PATTERN);
        java.util.regex.Matcher matcher = pattern.matcher(message);
        if (matcher.find()) {
            return matcher.group(1); // Group 1 contains the key value
        }
        return null;
    }
    public static void processSocketData(int xorKey) {
        // Establish socket connection to receive data
        try (Socket socket = new Socket("localhost", 12000);
             InputStream inputStream = socket.getInputStream()) {

            System.out.println("Connected to the socket on port 12000");

            // Wrap the input stream in a DataInputStream to read Java primitive data types
            DataInputStream dataInputStream = new DataInputStream(inputStream);

            // Read the first two bytes to determine the length of the data
            int length = dataInputStream.readUnsignedShort();

            System.out.println(length);
            // Read the rest of the data
            byte[] dataBytes = new byte[length];
            int totalBytesRead = 0;
            while (totalBytesRead < length) {
                int bytesRead = inputStream.read(dataBytes, totalBytesRead, length - totalBytesRead);
                if (bytesRead == -1) {
                    break;
                }
                totalBytesRead += bytesRead;
            }

            // Decrypt data using XOR cipher with key
            byte[] decryptedData = new byte[length];
            for (int i = 0; i < length; i++) {
                decryptedData[i] = (byte) (dataBytes[i] ^ xorKey);
            }

            // Convert decrypted data to ASCII string
            String decryptedString = new String(decryptedData, StandardCharsets.US_ASCII);
            System.out.println("Decrypted String: " + decryptedString);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

