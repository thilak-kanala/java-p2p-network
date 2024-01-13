import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BitfieldManager {
    private byte[] bitfield;

    public synchronized byte[] getBitfield() {
        return bitfield;
    }

    public synchronized void setBitfield(byte[] bitfield) {
        this.bitfield = bitfield;
    }

    int fileSize;
    int pieceSize;
    int numPieces;

    public int getNumPieces() {
        return numPieces;
    }

    public void setNumPieces(int numPieces) {
        this.numPieces = numPieces;
    }

    int bitFieldSize;

    public BitfieldManager(int fileSize, int pieceSize) {
        this.fileSize = fileSize;
        this.pieceSize = pieceSize;

        // Calculate the number of pieces
        numPieces = (int) Math.ceil((double) fileSize / pieceSize);

        // Calculate the size of the bitfield in bytes

        int bitFieldSize = (int) Math.ceil((double) numPieces / 8);
        this.bitFieldSize = bitFieldSize;
        bitfield = new byte[bitFieldSize];
    }

    // Fill all 1s in bitfield
    public synchronized void fillBitfield() {
        for (int i = 0; i < numPieces; i++) {
            setPiece(i);
        }
    }

    // Set a specific piece in the bitfield
    public synchronized void setPiece(int pieceIndex) {
        int byteIndex = pieceIndex / 8;
        int bitIndex = pieceIndex % 8;
        bitfield[byteIndex] |= (1 << (7 - bitIndex));
    }

    // Check if a specific piece is set in the bitfield
    public synchronized boolean hasPiece(int pieceIndex) {
        int byteIndex = pieceIndex / 8;
        int bitIndex = pieceIndex % 8;
        return (bitfield[byteIndex] & (1 << (7 - bitIndex))) != 0;
    }

    // Get the byte array representing the bitfield
    public synchronized byte[] getBitField() {
        return Arrays.copyOf(bitfield, bitfield.length);
    }

    public synchronized int getNumContainedPieces() {
        int totalContainedPieces = 0;
        for (int i = 0; i < numPieces; i++) {
            if (hasPiece(i)) {
                totalContainedPieces++;
            }
        }
        return totalContainedPieces;
    }

    public synchronized boolean hasCompelteFile() {
        if (getNumContainedPieces() == numPieces) {
            return true;
        } else {
            return false;
        }
    }

    public synchronized boolean interested(byte[] bitfield1, byte[] bitfield2) {
        // Check if the lengths of both bitfields are the same
        if (bitfield1.length != bitfield2.length) {
            throw new IllegalArgumentException("Bitfields must have the same length");
        }

        // Iterate through each byte in the bitfields
        for (int i = 0; i < bitfield1.length; i++) {
            // Use bitwise AND to find the pieces in bitfield2 that are not present in
            // bitfield1
            byte interestingPieces = (byte) (bitfield2[i] & ~bitfield1[i]);

            // If there are interesting pieces, return true
            if (interestingPieces != 0) {
                return true;
            }
        }

        // No interesting pieces found
        return false;
    }

    // Calculate missing pieces in bitfield2 compared to bitfield1
    public synchronized List<Integer> missingPieces(byte[] bitfield1, byte[] bitfield2) {
        // Check if the lengths of both bitfields are the same
        if (bitfield1.length != bitfield2.length) {
            throw new IllegalArgumentException("Bitfields must have the same length");
        }

        List<Integer> missingPieces = new ArrayList<>();

        // Iterate through each byte in the bitfields
        for (int i = 0; i < bitfield1.length; i++) {
            // Use bitwise AND to find the pieces in bitfield2 that are not present in
            // bitfield1
            byte interestingPieces = (byte) (bitfield2[i] & ~bitfield1[i]);

            // If there are interesting pieces, iterate through bits and add missing piece
            // indices
            if (interestingPieces != 0) {
                for (int j = 0; j < 8; j++) {
                    if ((interestingPieces & (1 << (7 - j))) != 0) {
                        int pieceIndex = i * 8 + j;
                        missingPieces.add(pieceIndex);
                    }
                }
            }
        }

        return missingPieces;
    }
}