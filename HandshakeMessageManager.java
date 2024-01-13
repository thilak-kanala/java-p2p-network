import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * The HandshakeMessageManager class is responsible for managing the creation and reading of handshake messages
 * in a peer-to-peer network.
 */
public class HandshakeMessageManager {

    // Constants
    String handshakeHeader;
    byte[] zeroBits;

    /**
     * Constructor that initializes the HandshakeMessageManager with default values.
     */
    public HandshakeMessageManager() {
        // Default values for handshake header and zero bits
        this.handshakeHeader = "P2PFILESHARINGPROJ";
        this.zeroBits = new byte[10];

        // Initialize zero bits to 0
        for (int i = 0; i < 10; i++) {
            zeroBits[i] = 0;
        }
    }

    /**
     * Generates a handshake message byte array for the given peer ID.
     *
     * @param peerId The peer ID to include in the handshake message.
     * @return The byte array representing the handshake message.
     */
    public byte[] generateHandshakeMessage(String peerId) {
        int integerPeerId = Integer.parseInt(peerId);

        ByteBuffer buffer = ByteBuffer.allocate(32);

        buffer.put(handshakeHeader.getBytes());
        buffer.put(zeroBits);
        buffer.putInt(integerPeerId);

        return buffer.array();
    }

    /**
     * Reads a handshake message from a ByteBuffer and returns a map containing the handshake components.
     *
     * @param buffer The ByteBuffer containing the handshake message.
     * @return A map containing the handshake components: "handshakeHeader", "zeroBits", and "peerId".
     */
    public Map<String, String> readHandshakeMessage(ByteBuffer buffer) {
        // Read handshake header
        byte[] headerBytes = new byte[18];
        buffer.get(headerBytes);
        String handshakeHeader = new String(headerBytes);

        // Read zero bits
        byte[] zeroBits = new byte[10];
        buffer.get(zeroBits);

        // Read peer ID
        String peerId = Integer.toString(buffer.getInt());

        // Create a map to store handshake components
        Map<String, String> handshakeMap = new HashMap<>();

        handshakeMap.put("handshakeHeader", handshakeHeader);
        handshakeMap.put("zeroBits", new String(zeroBits, StandardCharsets.UTF_8));
        handshakeMap.put("peerId", peerId);

        return handshakeMap;
    }

    // Getter and Setter methods for constants
    public String getHandshakeHeader() {
        return handshakeHeader;
    }

    public void setHandshakeHeader(String handshakeHeader) {
        this.handshakeHeader = handshakeHeader;
    }

    public byte[] getZeroBits() {
        return zeroBits;
    }

    public void setZeroBits(byte[] zeroBits) {
        this.zeroBits = zeroBits;
    }
}
