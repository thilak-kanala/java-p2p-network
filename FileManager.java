import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

public class FileManager {

    private String filePath;
    private int pieceSize;
    private int fileSize;
    private int numPieces;
    RandomAccessFile file;

    public FileManager(String filePath, int pieceSize, int fileSize) throws FileNotFoundException {
        this.filePath = filePath;
        this.pieceSize = pieceSize;
        this.fileSize = fileSize;
        this.numPieces = (int) Math.ceil((double) fileSize / pieceSize);
        ;
        this.file = new RandomAccessFile(filePath, "rw");
    }

    public synchronized void savePiece(byte[] data, int pieceIndex) {
        // // Check if the piece is already present in the file
        // if (bitfieldManager.hasPiece(pieceIndex)) {
        // System.out.println("Piece " + pieceIndex + " already present. Skipping
        // save.");
        // return;
        // }

        try {
            // Calculate the starting position in the file for the current piece
            int startPosition = pieceIndex * pieceSize;

            file.seek(startPosition);

            // Write bytes to the file
            file.write(data);

        } catch (IOException e) {
            // e.printStackTrace();
        }
    }

    public synchronized byte[] readPiece(int pieceIndex) {
        try {

            // Calculate the starting position in the file for the current piece
            long startPosition = (long) pieceIndex * pieceSize;

            file.seek(startPosition);

            byte[] data;

            if (pieceIndex == numPieces - 1) {
                data = new byte[fileSize % pieceSize];
            } else {
                data = new byte[pieceSize];
            }

            // Write bytes to the file
            file.readFully(data);

            return data;

        } catch (IOException e) {
            // System.out.println("synchronized readPiece");
            return null;
        }
    }
}
