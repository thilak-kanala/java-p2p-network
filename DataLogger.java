import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The PeerLogger class is responsible for logging various events in a
 * peer-to-peer network.
 */
public class DataLogger {
    private final String peerID;
    private final String logFilePath;

    /**
     * Constructor for PeerLogger class.
     *
     * @param peerId The ID of the peer for which logs will be generated.
     */
    public DataLogger(String peerId) {
        this.peerID = peerId;
        this.logFilePath = "./log_peer_" + peerId + ".log";

        File logFile = new File(logFilePath);
        try {
            if (logFile.exists()) {
                logFile.delete();
            }
            logFile.createNewFile();
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Get the current time in a formatted string.
     *
     * @return A string representing the current time.
     */
    private String getCurrentTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.format(new Date());
    }

    /**
     * Log a message to the specified log file.
     *
     * @param message The message to be logged.
     */
    private void log(String message) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFilePath, true))) {
            writer.println("[" + getCurrentTime() + "]: " + message);
            System.out.println("[" + getCurrentTime() + "]: " + message);
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    /**
     * Log a TCP connection event.
     *
     * @param connectedPeerID The ID of the peer to which a connection is made.
     */
    public void logTCPConnection(String connectedPeerID) {
        log("Peer " + peerID + " makes a connection to Peer " + connectedPeerID + ".");
    }

    /**
     * Log a connection event from another peer.
     *
     * @param connectedPeerID The ID of the peer from which a connection is made.
     */
    public void logConnectedFrom(String connectedPeerID) {
        log("Peer " + peerID + " is connected from Peer " + connectedPeerID + ".");
    }

    /**
     * Log a change in preferred neighbors.
     *
     * @param preferredNeighbors The list of preferred neighbors.
     */
    public void logPreferredNeighbors(List<String> preferredNeighbors) {
        log("Peer " + peerID + " has the preferred neighbors " + preferredNeighbors + ".");
    }

    // public void logPreferredNeighbors(List<String> preferredNeighbors, int size) {
    //     List<String> newList = new ArrayList<>();
    //     for (int i = 0; i < size; i++) {
    //         newList.add(preferredNeighbors.get(i));
    //     }
    //     log("Peer " + peerID + " has the preferred neighbors " + newList + ".");
    // }

    /**
     * Log a change in optimistically unchoked neighbor.
     *
     * @param optimisticUnchokedNeighborID The ID of the optimistically unchoked
     *                                     neighbor.
     */
    public void logOptimisticUnchokedNeighbor(String optimisticUnchokedNeighborID) {
        log("Peer " + peerID + " has the optimistically unchoked neighbor " + optimisticUnchokedNeighborID + ".");
    }

    /**
     * Log an unchoking event.
     *
     * @param unchokedPeerID The ID of the peer that unchokes this peer.
     */
    public void logUnchoking(String unchokedPeerID) {
        log("Peer " + peerID + " is unchoked by " + unchokedPeerID + ".");
    }

    /**
     * Log a choking event.
     *
     * @param chokedPeerID The ID of the peer that chokes this peer.
     */
    public void logChoking(String chokedPeerID) {
        log("Peer " + peerID + " is choked by " + chokedPeerID + ".");
    }

    /**
     * Log the receipt of a 'have' message.
     *
     * @param senderPeerID The ID of the peer that sent the 'have' message.
     * @param pieceIndex   The piece index contained in the message.
     */
    public void logReceivedHave(String senderPeerID, int pieceIndex) {
        log("Peer " + peerID + " received the 'have' message from " + senderPeerID + " for the piece " + pieceIndex
                + ".");
    }

    /**
     * Log the receipt of an 'interested' message.
     *
     * @param senderPeerID The ID of the peer that sent the 'interested' message.
     */
    public void logReceivedInterested(String senderPeerID) {
        log("Peer " + peerID + " received the 'interested' message from " + senderPeerID + ".");
    }

    /**
     * Log the receipt of a 'not interested' message.
     *
     * @param senderPeerID The ID of the peer that sent the 'not interested'
     *                     message.
     */
    public void logReceivedNotInterested(String senderPeerID) {
        log("Peer " + peerID + " received the 'not interested' message from " + senderPeerID + ".");
    }

    /**
     * Log the completion of downloading a piece.
     *
     * @param senderPeerID   The ID of the peer that sent the piece.
     * @param pieceIndex     The piece index downloaded.
     * @param numberOfPieces The current number of pieces the peer has.
     */
    public void logDownloadedPiece(String senderPeerID, int pieceIndex, int numberOfPieces) {
        log("Peer " + peerID + " has downloaded the piece " + pieceIndex + " from " + senderPeerID +
                ". Now the number of pieces it has is " + numberOfPieces + ".");
    }

    /**
     * Log the completion of downloading the complete file.
     */
    public void logCompletionOfDownload() {
        log("Peer " + peerID + " has downloaded the complete file.");
    }
}
