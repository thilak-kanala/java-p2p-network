/**
 * The MessageManager class is responsible for managing
 * actual messages and handshake messages.
 */
public class MessageManager {

    ActualMessageManager actualMessageManager;
    HandshakeMessageManager handshakeMessageManager;

    public MessageManager() {
        this.actualMessageManager = new ActualMessageManager();
        this.handshakeMessageManager = new HandshakeMessageManager();
    }

    public ActualMessageManager getActualMessageManager() {
        return actualMessageManager;
    }

    public void setActualMessageManager(ActualMessageManager actualMessageManager) {
        this.actualMessageManager = actualMessageManager;
    }

    public HandshakeMessageManager getHandshakeMessageManager() {
        return handshakeMessageManager;
    }

    public void setHandshakeMessageManager(HandshakeMessageManager handshakeMessageManager) {
        this.handshakeMessageManager = handshakeMessageManager;
    }

}
