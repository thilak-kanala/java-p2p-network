import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class PeerStatus {
    private BitfieldManager bitfieldManager;
    private boolean choked;
    private boolean interested;
    private boolean requestedPiece;
    private String clientPeerID;
    private Socket socket;
    private InputStream in;
    private OutputStream out;
    private Set<Integer> requestedPieces;
    private int piecesDownloadSincePreviousChokingInterval;
    private List<String> chokedList;

    public List<String> getChokedList() {
        return chokedList;
    }

    public void setChokedList(List<String> chokedList) {
        this.chokedList.clear();
        for (String peer : chokedList) {
            this.chokedList.add(peer);
        }
    }

    public void addToChokedList(String item) {
        if (!this.chokedList.contains(item))
        {
            this.chokedList.add(item);
        }
    }

    public PeerStatus(int fileSize, int pieceSize) {
        bitfieldManager = new BitfieldManager(fileSize, pieceSize);
        interested = false;
        requestedPieces = new HashSet<>();
        choked = false;
        chokedList = new ArrayList<>();
    }

    public int getPiecesDownloadSincePreviousChokingInterval() {
        return piecesDownloadSincePreviousChokingInterval;
    }

    public void setPiecesDownloadSincePreviousChokingInterval(int piecesDownloadSincePreviousChokingInterval) {
        this.piecesDownloadSincePreviousChokingInterval = piecesDownloadSincePreviousChokingInterval;
    }

    public void incrementPiecesDownloadSincePreviousChokingInterval() {
        piecesDownloadSincePreviousChokingInterval++;
    }

    public synchronized Set<Integer> getRequestedPieces() {
        return requestedPieces;
    }

    public synchronized void setRequestedPieces(Set<Integer> requestedPieces) {
        this.requestedPieces = requestedPieces;
    }

    public synchronized void addRequestedPiece(int piece) {
        requestedPieces.add(piece);
    }

    public synchronized boolean isRequestedPiece(int piece) {
        return requestedPieces.contains(piece);
    }

    public synchronized InputStream getIn() {
        return in;
    }

    public synchronized void setIn(InputStream in) {
        this.in = in;
    }

    public synchronized OutputStream getOut() {
        return out;
    }

    public synchronized void setOut(OutputStream out) {
        this.out = out;
    }

    public synchronized Socket getSocket() {
        return socket;
    }

    public synchronized void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getClientPeerID() {
        return clientPeerID;
    }

    public void setClientPeerID(String clientPeerID) {
        this.clientPeerID = clientPeerID;
    }

    public synchronized boolean isRequestedPiece() {
        return requestedPiece;
    }

    public synchronized void setRequestedPiece(boolean requestedPiece) {
        this.requestedPiece = requestedPiece;
    }

    public synchronized boolean isInterested() {
        return interested;
    }

    public synchronized void setInterested(boolean interested) {
        this.interested = interested;
    }

    public boolean isChoked() {
        return choked;
    }

    public void setChoked(boolean choked) {
        this.choked = choked;
    }

    public synchronized BitfieldManager getBitfieldManager() {
        return bitfieldManager;
    }

    public synchronized void setBitfieldManager(BitfieldManager bitfield) {
        this.bitfieldManager = bitfield;
    }

    public synchronized void fillBitfield() {
        bitfieldManager.fillBitfield();
    }

    @Override
    public String toString() {
        return "PeerStatus [bitfieldManager=" + bitfieldManager + ", choked=" + choked + ", interested=" + interested
                + ", requestedPiece=" + requestedPiece + "]";
    }

    // public PeerStatus()
    // {

    // }
}
