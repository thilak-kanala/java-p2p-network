import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The PeerInfoConfigParser class is responsible for parsing the PeerInfo configuration file,
 * creating a list of Peer objects, and generating a map for easy lookup by peer ID.
 */
public class PeerInfoConfigParser {

    // List to store Peer objects
    List<Peer> peers;

    // Map for easy lookup of peers by peer ID
    Map<String, Peer> peerMap;

    /**
     * Constructor that parses the PeerInfo configuration file and initializes the list of Peer objects
     * and the map for easy lookup.
     *
     * @param peerInfoConfigFileName The name of the PeerInfo configuration file to parse.
     */
    public PeerInfoConfigParser(String peerInfoConfigFileName) {
        this.peers = parseInput(peerInfoConfigFileName);
        this.peerMap = createPeerMap(this.peers);
    }

    /**
     * Generates a string representation of the PeerInfoConfigParser, displaying each Peer's information.
     *
     * @return A string containing the information of each Peer.
     */
    public String toString() {
        StringBuilder result = new StringBuilder();

        for (Peer peer : peers) {
            result.append(peer.getPeerId())
                    .append(" ")
                    .append(peer.getHostName())
                    .append(" ")
                    .append(peer.getListeningPort())
                    .append(" ")
                    .append(peer.hasFile())
                    .append("\n");
        }
        return result.toString();
    }

    /**
     * Creates a map from peer ID to its corresponding Peer object, allowing for easy lookup.
     *
     * @param peers The list of Peer objects to create the map from.
     * @return A map where keys are peer IDs and values are corresponding Peer objects.
     */
    public Map<String, Peer> createPeerMap(List<Peer> peers) {
        Map<String, Peer> peerMap = new ConcurrentHashMap<>();

        for (Peer peer : peers) {
            peerMap.put(peer.getPeerId(), peer);
        }

        return peerMap;
    }

    /**
     * Parses the PeerInfo configuration file and updates the list of peers.
     *
     * @param peerInfoConfigFileName The name of the PeerInfo configuration file to parse.
     * @return A list of Peer objects created from the parsed data.
     */
    public List<Peer> parseInput(String peerInfoConfigFileName) {
        List<Peer> peers = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(peerInfoConfigFileName))) {
                String line;
                while ((line = br.readLine()) != null) {
                    String[] parts = line.split("\\s+");
                    if (parts.length == 4) {
                        String peerId = parts[0];
                        String hostName = parts[1];
                        int listeningPort = Integer.parseInt(parts[2]);
                        boolean hasFile = Integer.parseInt(parts[3]) == 1;
                        peers.add(new Peer(peerId, hostName, listeningPort, hasFile));
                    } else {
                        System.out.println("Invalid input line: " + line);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }

        return peers;
    }

    /**
     * The main method for independent class testing.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        PeerInfoConfigParser peerConfigInfo = new PeerInfoConfigParser("PeerInfo.cfg");
        System.out.println(peerConfigInfo);
    }
}
