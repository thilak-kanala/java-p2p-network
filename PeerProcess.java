import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

class CalculatePreferredNeighboursTask implements Runnable {

    // Configuration and information parsers
    CommonConfigParser commonConfigInfo;
    PeerInfoConfigParser peerConfigInfo;

    // Message manager for handling communication messages
    MessageManager messageManager;

    // Data logger for logging peer activities
    DataLogger logger;

    // Self peer information
    Peer self;

    // Map from peerID to it's corresponding PeerStatus class instance
    Map<String, PeerStatus> peerStatusMap;

    FileManager fileManager;

    public CalculatePreferredNeighboursTask(CommonConfigParser commonConfigInfo,
            PeerInfoConfigParser peerConfigInfo, MessageManager messageManager, DataLogger logger, Peer self,
            Map<String, PeerStatus> peerStatusMap, FileManager fileManager) {

        this.commonConfigInfo = commonConfigInfo;
        this.peerConfigInfo = peerConfigInfo;
        this.messageManager = messageManager;
        this.logger = logger;
        this.self = self;
        this.peerStatusMap = peerStatusMap;
        this.fileManager = fileManager;
    }

    public synchronized void write(OutputStream out, byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }

    public synchronized void read(InputStream in, byte[] bytes) throws IOException {
        in.read(bytes);
    }

    // Helper method to sort a map by its values
    private static Map<String, Float> sortByValue(Map<String, Float> map) {
        List<Map.Entry<String, Float>> list = new ArrayList<>(map.entrySet());
        list.sort((entry1, entry2) -> entry1.getValue().compareTo(entry2.getValue()));

        Map<String, Float> sortedMap = new LinkedHashMap<>();
        for (Map.Entry<String, Float> entry : list) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        return sortedMap;
    }

    // Method to pick k random elements from a list
    private static <T> List<T> pickRandomElements(List<T> list, int k) {
        if (k > list.size()) {
            throw new IllegalArgumentException("Cannot pick more elements than the list contains.");
        }

        List<T> result = new ArrayList<>();
        Random random = new Random();

        for (int i = 0; i < k; i++) {
            int randomIndex = random.nextInt(list.size());
            result.add(list.remove(randomIndex));
        }

        return result;
    }

    @Override
    public void run() {

        // If peer conatins complete data, k preferred neighbour are picked randomly
        // from choked and interested peers
        try {

            List<String> unchokedList = new ArrayList<>();
            List<String> chokedList = new ArrayList<>();

            List<String> chokedAndInterested = new ArrayList<>();

            for (Peer peer : peerConfigInfo.peers) {
                // Skip if peer is self, or not interested
                if (peer.getPeerId() == self.getPeerId() ||
                        !peerStatusMap.get(peer.getPeerId()).isInterested()) {
                    continue;
                }

                chokedAndInterested.add(peer.getPeerId());
            }

            if (chokedAndInterested.size() < commonConfigInfo.numberOfPreferredNeighbors) {
                // Unchoke
                for (String peerID : chokedAndInterested) {
                    peerStatusMap.get(peerID).setChoked(false);
                    unchokedList.add(peerID);
                    OutputStream out = peerStatusMap.get(peerID).getOut();
                    byte[] actualMessage = messageManager.actualMessageManager.generateActualMessage(
                            1, (byte) 1, null);
                    write(out, actualMessage);
                }
            } else {
                List<String> chokedAndInterestedPeers = pickRandomElements(chokedAndInterested,
                        commonConfigInfo.numberOfPreferredNeighbors);

                for (Peer peer : peerConfigInfo.peers) {

                    if (peer.getPeerId() == self.getPeerId()) {
                        continue;
                    }

                    // Unchoke
                    if (chokedAndInterestedPeers.contains(peer.getPeerId())) {
                        peerStatusMap.get(peer.getPeerId()).setChoked(false);
                        unchokedList.add(peer.getPeerId());
                        OutputStream out = peerStatusMap.get(peer.getPeerId()).getOut();
                        byte[] actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                1, (byte) 1, null);
                        write(out, actualMessage);
                    }
                    // Choke
                    else {
                        chokedList.add(peer.getPeerId());
                        peerStatusMap.get(peer.getPeerId()).setChoked(true);
                        OutputStream out = peerStatusMap.get(peer.getPeerId()).getOut();
                        byte[] actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                1, (byte) 0, null);
                        write(out, actualMessage);
                    }
                }

            }
            peerStatusMap.get(self.getPeerId()).setChokedList(chokedList);
            logger.logPreferredNeighbors(unchokedList);
        } catch (IOException e) {
            // e.printStackTrace();
        }
    }
}

class CalculateOptimisticallyUnchokedNeighbour implements Runnable {

    // Configuration and information parsers
    CommonConfigParser commonConfigInfo;
    PeerInfoConfigParser peerConfigInfo;

    // Message manager for handling communication messages
    MessageManager messageManager;

    // Data logger for logging peer activities
    DataLogger logger;

    // Self peer information
    Peer self;

    // Map from peerID to it's corresponding PeerStatus class instance
    Map<String, PeerStatus> peerStatusMap;

    FileManager fileManager;

    public CalculateOptimisticallyUnchokedNeighbour(CommonConfigParser commonConfigInfo,
            PeerInfoConfigParser peerConfigInfo, MessageManager messageManager, DataLogger logger, Peer self,
            Map<String, PeerStatus> peerStatusMap, FileManager fileManager) {
        this.commonConfigInfo = commonConfigInfo;
        this.peerConfigInfo = peerConfigInfo;
        this.messageManager = messageManager;
        this.logger = logger;
        this.self = self;
        this.peerStatusMap = peerStatusMap;
        this.fileManager = fileManager;
    }

    public synchronized void write(OutputStream out, byte[] bytes) throws IOException {
        out.write(bytes);
        out.flush();
    }

    public synchronized void read(InputStream in, byte[] bytes) throws IOException {
        in.read(bytes);
    }

    @Override
    public void run() {

        try {

            List<String> unchokedList = new ArrayList<>();
            List<String> chokedList = new ArrayList<>();

            List<String> chokedAndInterested = new ArrayList<>();

            for (Peer peer : peerConfigInfo.peers) {
                // Skip if peer is self, or not interested
                if (peer.getPeerId() == self.getPeerId() ||
                        !peerStatusMap.get(peer.getPeerId()).isInterested()) {
                    continue;
                }

                chokedAndInterested.add(peer.getPeerId());

                OutputStream out = peerStatusMap.get(peer.getPeerId()).getOut();
                byte[] actualMessage = messageManager.actualMessageManager.generateActualMessage(
                        1, (byte) 1, null);
                write(out, actualMessage);

                peerStatusMap.get(self.getPeerId()).addToChokedList(peer.getPeerId());
                logger.logOptimisticUnchokedNeighbor(peer.getPeerId());

                break;
            }

        } catch (IOException e) {
            // e.printStackTrace();
        }
    }
}

/**
 * The PeerProcess class represents the main entry point for a peer in the P2P
 * file-sharing system.
 */
public class PeerProcess {

    // Configuration and information parsers
    CommonConfigParser commonConfigInfo;
    PeerInfoConfigParser peerConfigInfo;

    // Message manager for handling communication messages
    MessageManager messageManager;

    // Data logger for logging peer activities
    DataLogger logger;

    // Self peer information
    Peer self;

    // Map from peerID to it's corresponding PeerStatus class instance
    Map<String, PeerStatus> peerStatusMap;

    FileManager fileManager;

    /**
     * Constructor for PeerProcess.
     *
     * @param peerID The peer ID of the current peer.
     * @throws FileNotFoundException
     */
    public PeerProcess(String peerID) throws FileNotFoundException {
        commonConfigInfo = new CommonConfigParser("Common.cfg");
        peerConfigInfo = new PeerInfoConfigParser("PeerInfo.cfg");
        messageManager = new MessageManager();

        fileManager = new FileManager("./" + peerID + "/" + commonConfigInfo.fileName,
                commonConfigInfo.pieceSize, commonConfigInfo.pieceSize);

        this.self = peerConfigInfo.peerMap.get(peerID);

        // Initialize the map that maps from peerID to it's corresponding bitfield
        peerStatusMap = new ConcurrentHashMap<>();

        for (Peer peer : peerConfigInfo.peers) {
            peerStatusMap.put(peer.getPeerId(), new PeerStatus(commonConfigInfo.fileSize, commonConfigInfo.pieceSize));
        }

        if (self.hasFile()) {
            peerStatusMap.get(self.getPeerId()).fillBitfield();
        }
    }

    /**
     * Assigns a data logger to the current peer for logging activities.
     *
     * @param peerId The peer ID for which to assign the data logger.
     */
    public void assignDataLogger(String peerId) {
        this.logger = new DataLogger(peerId);
    }

    /**
     * Starts the server port to listen for incoming connections from other peers.
     */
    void startServerPort() {
        try (ServerSocket server = new ServerSocket(self.getListeningPort())) {
            while (true) {
                try {
                    Socket socket = server.accept();

                    Thread task = new Thread(new PeerConnectionHandler(socket,
                            commonConfigInfo,
                            peerConfigInfo,
                            messageManager,
                            logger,
                            self,
                            peerStatusMap,
                            fileManager));
                    task.start();

                } catch (IOException ex) {
                }
            }
        } catch (IOException ex) {
            // System.err.println("Couldn't start server: " + ex);
        }
    }

    /**
     * Starts connections with other peers in the network.
     */
    void startConnectionWithOtherPeers() {
        for (Peer peer : peerConfigInfo.peers) {
            if (peer.getPeerId() == self.getPeerId()) {
                break; // Stop making connections when the current peer ID is reached
            }

            // Data Logging: a peer establishes a TCP connection to another peer
            logger.logTCPConnection((peer.getPeerId()));

            try {
                Socket socket = new Socket(peer.getHostName(), peer.getListeningPort());

                InputStream in = new BufferedInputStream(socket.getInputStream());
                OutputStream out = new BufferedOutputStream(socket.getOutputStream());

                peerStatusMap.get(self.getPeerId()).setSocket(socket);
                peerStatusMap.get(self.getPeerId()).setIn(in);
                peerStatusMap.get(self.getPeerId()).setOut(out);

                byte[] handshake = messageManager.handshakeMessageManager.generateHandshakeMessage(self.getPeerId());
                out.write(handshake);
                out.flush();

                byte[] response = new byte[32];
                in.read(response);

                ByteBuffer buffer = ByteBuffer.wrap(response);

                Map<String, String> responseToHandshake = messageManager.handshakeMessageManager
                        .readHandshakeMessage(buffer);

                // Validate handshake correctness
                byte[] tempZeroBits = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
                String tempZeroBitsString = new String(tempZeroBits);

                if (!(responseToHandshake.get("handshakeHeader").equals("P2PFILESHARINGPROJ"))
                        || !(responseToHandshake.get("zeroBits").equals(tempZeroBitsString))
                        || !(responseToHandshake.get("peerId").equals(peer.getPeerId()))) {
                    // Incorrect Handshake, break the loop
                    break;
                }

                Thread senderTask = new Thread(new SenderThread(socket,
                        in, out,
                        commonConfigInfo,
                        peerConfigInfo,
                        messageManager,
                        logger,
                        self,
                        peer.getPeerId(),
                        peerStatusMap,
                        fileManager));
                senderTask.start();
            } catch (IOException e) {
                // e.printStackTrace();
            }
        }
    }

    /**
     * The main method for the PeerProcess class.
     *
     * @param args Command-line arguments.
     * @throws FileNotFoundException
     */
    public static void main(String[] args) throws FileNotFoundException {
        String inputPeerID = args[0];

        PeerProcess peer = new PeerProcess(inputPeerID);
        peer.assignDataLogger(inputPeerID);

        System.out.println("== COMMON CONFIGURATION ==");
        System.out.println(peer.commonConfigInfo.toString());

        System.out.println("== PEER CONFIGURATION ==");
        System.out.println(peer.peerConfigInfo.toString());

        peer.startConnectionWithOtherPeers();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        CalculatePreferredNeighboursTask calculatePreferredNeighboursTask;
        calculatePreferredNeighboursTask = new CalculatePreferredNeighboursTask(
                peer.commonConfigInfo,
                peer.peerConfigInfo,
                peer.messageManager,
                peer.logger,
                peer.self,
                peer.peerStatusMap,
                peer.fileManager);

        scheduler.scheduleAtFixedRate(calculatePreferredNeighboursTask, 0,
                peer.commonConfigInfo.unchokingInterval,
                TimeUnit.SECONDS);

        CalculateOptimisticallyUnchokedNeighbour calculateOptimisticallyUnchokedNeighbour;

        calculateOptimisticallyUnchokedNeighbour = new CalculateOptimisticallyUnchokedNeighbour(
                peer.commonConfigInfo,
                peer.peerConfigInfo,
                peer.messageManager,
                peer.logger,
                peer.self,
                peer.peerStatusMap,
                peer.fileManager);

        scheduler.scheduleAtFixedRate(calculateOptimisticallyUnchokedNeighbour, 0,
                peer.commonConfigInfo.optimisticUnchokingInterval, TimeUnit.SECONDS);

        peer.startServerPort();

    }
}
