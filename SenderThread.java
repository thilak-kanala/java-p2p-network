import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SenderThread implements Runnable {

    Socket socket;
    InputStream in;
    OutputStream out;

    // Configuration and information parsers
    CommonConfigParser commonConfigInfo;
    PeerInfoConfigParser peerConfigInfo;

    // Message manager for handling communication messages
    MessageManager messageManager;

    // Data logger for logging peer activities
    DataLogger logger;

    // Self peer information
    Peer self;

    String clientPeerID;

    // Map from peerID to it's corresponding PeerStatus class instance
    Map<String, PeerStatus> peerStatusMap;

    FileManager fileManager;

    public SenderThread(Socket socket, InputStream in, OutputStream out, CommonConfigParser commonConfigInfo,
            PeerInfoConfigParser peerConfigInfo, MessageManager messageManager, DataLogger logger, Peer self,
            String clientPeerID, Map<String, PeerStatus> peerStatusMap, FileManager fileManager) {
        this.socket = socket;
        this.in = in;
        this.out = out;
        this.commonConfigInfo = commonConfigInfo;
        this.peerConfigInfo = peerConfigInfo;
        this.messageManager = messageManager;
        this.logger = logger;
        this.self = self;
        this.clientPeerID = clientPeerID;
        this.peerStatusMap = peerStatusMap;
        this.fileManager = fileManager;
    }

    boolean allPeersDownloadCompleted() {
        for (Peer peer : peerConfigInfo.peers) {
            if (!peerStatusMap.get(peer.getPeerId()).getBitfieldManager().hasCompelteFile()) {
                return false;
            }
        }
        return true;
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
            // Send self bitfield
            byte[] bitfield = peerStatusMap.get(self.getPeerId()).getBitfieldManager().getBitField();

            byte[] actualMessage = messageManager.actualMessageManager.generateActualMessage(
                    bitfield.length + 1, (byte) 5, bitfield);

            write(out, actualMessage);

            // Receive actualMessage (BITFIELD)
            byte[] message_length_buffer = new byte[4];
            read(in, message_length_buffer);
            ByteBuffer message_length_byte_buffer = ByteBuffer.wrap(message_length_buffer);
            int message_length = message_length_byte_buffer.getInt();

            byte[] message_type_buffer = new byte[1];
            read(in, message_type_buffer);

            ByteBuffer message_type_byte_buffer = ByteBuffer.wrap(message_type_buffer);
            int message_type = (int) message_type_byte_buffer.get();

            byte[] message_payload = new byte[message_length - 1];
            read(in, message_payload);

            // Actions to perform based on received message
            if (message_type == 5) { // BITFIELD

                // Update peer bitfeild map
                peerStatusMap.get(clientPeerID).getBitfieldManager().setBitfield(message_payload);

                // Verify if interested
                byte[] bitfield1 = peerStatusMap.get(self.getPeerId()).getBitfieldManager().getBitField();
                byte[] bitfield2 = peerStatusMap.get(clientPeerID).getBitfieldManager().getBitField();

                if (peerStatusMap.get(self.getPeerId()).getBitfieldManager().interested(
                        bitfield1,
                        bitfield2)) {

                    // Send interested message
                    actualMessage = messageManager.actualMessageManager.generateActualMessage(
                            1, (byte) 2, null);

                    write(out, actualMessage);

                } else {
                    // Send not interested message
                    actualMessage = messageManager.actualMessageManager.generateActualMessage(
                            1, (byte) 3, null);

                    write(out, actualMessage);
                }
            }

            while (!allPeersDownloadCompleted()) {

                if (in.available() == 0) {
                    continue;
                }

                // Receive actualMessage
                message_length_buffer = new byte[4];
                read(in, message_length_buffer);

                message_length_byte_buffer = ByteBuffer.wrap(message_length_buffer);
                message_length = message_length_byte_buffer.getInt();

                message_type_buffer = new byte[1];
                read(in, message_type_buffer);

                message_type_byte_buffer = ByteBuffer.wrap(message_type_buffer);
                message_type = (int) message_type_byte_buffer.get();

                message_payload = new byte[message_length - 1];
                read(in, message_payload);

                // ByteBuffer message_payload_byte_buffer = ByteBuffer.wrap(message_payload);

                if (message_type == 0) // Choke
                {
                    logger.logChoking(clientPeerID);
                } else if (message_type == 1) // Unchoke
                {
                    logger.logUnchoking(clientPeerID);

                    for (int i = 0; i < 1; i++) {
                        // Send request message if required
                        byte[] bitfield1 = peerStatusMap.get(self.getPeerId()).getBitfieldManager().getBitField();
                        byte[] bitfield2 = peerStatusMap.get(clientPeerID).getBitfieldManager().getBitField();

                        List<Integer> missingPieces = peerStatusMap.get(self.getPeerId()).getBitfieldManager()
                                .missingPieces(bitfield1, bitfield2);

                        if (missingPieces.size() > 0) {

                            Random rand = new Random();
                            int randIndex;
                            do {
                                randIndex = rand.nextInt(missingPieces.size());
                            } while (peerStatusMap.get(self.getPeerId())
                                    .isRequestedPiece(missingPieces.get(randIndex)));
                            peerStatusMap.get(self.getPeerId()).addRequestedPiece(missingPieces.get(randIndex));

                            // Send request message
                            actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                    5, (byte) 6,
                                    ByteBuffer.allocate(4).putInt(missingPieces.get(randIndex)).array());

                            write(out, actualMessage);

                            // Read the received piece message
                            message_length_buffer = new byte[4];
                            read(in, message_length_buffer);

                            message_length_byte_buffer = ByteBuffer.wrap(message_length_buffer);
                            message_length = message_length_byte_buffer.getInt();

                            message_type_buffer = new byte[1];
                            read(in, message_type_buffer);

                            message_type_byte_buffer = ByteBuffer.wrap(message_type_buffer);
                            message_type = (int) message_type_byte_buffer.get();

                            message_payload = new byte[message_length - 1];
                            read(in, message_payload);

                            fileManager.savePiece(message_payload, missingPieces.get(randIndex));

                            peerStatusMap.get(self.getPeerId()).getBitfieldManager()
                                    .setPiece(missingPieces.get(randIndex));

                            int hasNumPieces = peerStatusMap.get(self.getPeerId()).getBitfieldManager()
                                    .getNumContainedPieces();

                            // Data Logging
                            logger.logDownloadedPiece(clientPeerID, missingPieces.get(randIndex),
                                    hasNumPieces);

                            if (peerStatusMap.get(self.getPeerId()).getBitfieldManager().hasCompelteFile()) {
                                logger.logCompletionOfDownload();
                            }

                            // Send not interested to appropriate peers
                            for (Peer peer : peerConfigInfo.peers) {

                                OutputStream out2 = peerStatusMap.get(peer.getPeerId()).getOut();

                                if (peer.getPeerId() == self.getPeerId() || out2 == null) {
                                    continue;
                                }

                                byte[] bitfield3 = peerStatusMap.get(self.getPeerId()).getBitfieldManager()
                                        .getBitField();
                                byte[] bitfield4 = peerStatusMap.get(clientPeerID).getBitfieldManager().getBitField();

                                // send have message
                                actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                        5, (byte) 4,
                                        ByteBuffer.allocate(4).putInt(missingPieces.get(randIndex)).array());
                                write(out2, actualMessage);

                                // send not interested
                                if (!peerStatusMap.get(self.getPeerId()).getBitfieldManager().interested(bitfield3,
                                        bitfield4)) {
                                    actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                            1, (byte) 3, null);

                                    write(out2, actualMessage);
                                }
                            }
                        }
                    }

                } else if (message_type == 2) // Interested
                {
                    /*
                     * Data Logging: receiving ‘interested’ message
                     */
                    logger.logReceivedInterested(clientPeerID);

                    peerStatusMap.get(clientPeerID).setInterested(true);

                } else if (message_type == 3) // Not Interested
                {
                    /*
                     * Data Logging: receiving ‘interested’ message
                     */
                    logger.logReceivedNotInterested(clientPeerID);

                    peerStatusMap.get(clientPeerID).setInterested(false);

                } else if (message_type == 4) { // have message
                    
                    ByteBuffer message_payload_byte_buffer = ByteBuffer.wrap(message_payload);
                    int havePiece = message_payload_byte_buffer.getInt();

                    logger.logReceivedHave(clientPeerID, havePiece);

                    // send interested message if it does not have the piece
                    if (!peerStatusMap.get(self.getPeerId()).getBitfieldManager().hasPiece(havePiece)) {
                        // Send interested message
                        actualMessage = messageManager.actualMessageManager.generateActualMessage(
                                1, (byte) 2, null);

                        write(out, actualMessage);
                    }

                } else if (message_type == 6) // Request message
                {
                    // Send the requested piece
                    ByteBuffer message_payload_byte_buffer = ByteBuffer.wrap(message_payload);
                    int requestedPiece = message_payload_byte_buffer.getInt();

                    byte[] piece_payload = fileManager.readPiece(requestedPiece);

                    actualMessage = messageManager.actualMessageManager.generateActualMessage(
                            piece_payload.length + 1, (byte) 7, piece_payload);

                    peerStatusMap.get(clientPeerID).incrementPiecesDownloadSincePreviousChokingInterval();

                    write(out, actualMessage);
                }
            }

        } catch (IOException e) {
            // System.out.println("peer Communication");
        }

    }
}