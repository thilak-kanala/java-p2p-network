import java.nio.ByteBuffer;

/**
 * The ActualMessageManager class is responsible for managing the creation and
 * reading of actual messages
 * in a peer-to-peer network.
 */
public class ActualMessageManager {

    /**
     * Generates an actual message byte array from the message length, type, and
     * payload.
     *
     * @return The byte array representing the actual message.
     */
    public byte[] generateActualMessage(int messageLength, byte messageType, byte[] messagePayload) {
        // /*
        // * message length = 1 byte message type + length of message payload
        // */
        // messageLength = 1 + messagePayload.length;

        // int bufferLength = messageLength + 4;

        ByteBuffer buffer = ByteBuffer.allocate(messageLength + 4);
        buffer.putInt(messageLength);
        buffer.put(messageType);

        if (!(messagePayload == null)) {
            buffer.put(messagePayload);
        }

        return buffer.array();
    }

    /**
     * Reads an actual message from a ByteBuffer and returns a string representation
     * of the message.
     *
     * @param buffer The ByteBuffer containing the actual message.
     * @return A string representation of the actual message.
     */
    public String readActualMessage(ByteBuffer message_length, ByteBuffer message_payload) {

        // Read message length
        int length = message_length.getInt();

        // Read message type
        byte message_type = message_payload.get();

        // Read payload
        byte[] payload = new byte[length];
        message_payload.get(payload);

        StringBuilder result = new StringBuilder();

        result.append(message_type)
                .append(new String(payload));

        return result.toString();

        // // Print message information (for testing)
        // System.out.println("::Actual Message::");
        // System.out.println("Type: " + messageType);
        // System.out.println("Payload: " + new String(payload));
    }

}