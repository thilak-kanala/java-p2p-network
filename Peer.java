public class Peer {
    private String peerId;
    private String hostName;
    private int listeningPort;
    private boolean hasFile;

    public Peer(String peerId, String hostName, int listeningPort, boolean hasFile) {
        this.peerId = peerId;
        this.hostName = hostName;
        this.listeningPort = listeningPort;
        this.hasFile = hasFile;
    }

    public String getPeerId() {
        return peerId;
    }

    public void setPeerId(String peerId) {
        this.peerId = peerId;
    }

    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public int getListeningPort() {
        return listeningPort;
    }

    public void setListeningPort(int listeningPort) {
        this.listeningPort = listeningPort;
    }

    public boolean hasFile() {
        return hasFile;
    }

    public void setHasFile(boolean hasFile) {
        this.hasFile = hasFile;
    }

    
}