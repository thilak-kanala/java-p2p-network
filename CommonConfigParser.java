import java.io.BufferedReader;
import java.io.FileReader;

/**
 * The CommonConfigParser class is responsible for parsing a common configuration file
 * and providing access to the configuration parameters.
 */
public class CommonConfigParser {

    // Configuration parameters
    int numberOfPreferredNeighbors;
    int unchokingInterval;
    int optimisticUnchokingInterval;
    String fileName;
    int fileSize;
    int pieceSize;

    /**
     * Constructor that initializes the CommonConfigParser by parsing the provided common configuration file.
     *
     * @param commonConfigFileName The name of the common configuration file to parse.
     */
    public CommonConfigParser(String commonConfigFileName) {
        parseInput(commonConfigFileName);
    }

    /**
     * Parses the common configuration file and updates the relevant configuration parameters.
     *
     * @param commonConfigFileName The name of the common configuration file to parse.
     */
    public void parseInput(String commonConfigFileName) {
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(commonConfigFileName))) {
                String line;
                int lineNumber = 0;
                while ((line = br.readLine()) != null) {
                    lineNumber += 1;
                    String[] parts = line.split("\\s+");

                    // Update configuration parameters based on line number
                    switch (lineNumber) {
                        case 1:
                            numberOfPreferredNeighbors = Integer.parseInt(parts[1]);
                            break;
                        case 2:
                            unchokingInterval = Integer.parseInt(parts[1]);
                            break;
                        case 3:
                            optimisticUnchokingInterval = Integer.parseInt(parts[1]);
                            break;
                        case 4:
                            fileName = parts[1];
                            break;
                        case 5:
                            fileSize = Integer.parseInt(parts[1]);
                            break;
                        case 6:
                            pieceSize = Integer.parseInt(parts[1]);
                            break;
                    }
                }
            }
        } catch (Exception e) {
            // System.out.println("Exception: " + e);
        }
    }

    /**
     * Returns a string representation of the CommonConfigParser object.
     *
     * @return A string containing the configuration parameters.
     */
    public String toString() {
        StringBuilder result = new StringBuilder();

        result.append("numberOfPreferredNeighbors: ")
                .append(numberOfPreferredNeighbors)
                .append("\n")
                .append("unchokingInterval: ")
                .append(unchokingInterval)
                .append("\n")
                .append("optimisticUnchokingInterval: ")
                .append(optimisticUnchokingInterval)
                .append("\n")
                .append("fileName: ")
                .append(fileName)
                .append("\n")
                .append("fileSize: ")
                .append(fileSize)
                .append("\n")
                .append("pieceSize: ")
                .append(pieceSize)
                .append("\n");

        return result.toString();
    }

    // Getter and Setter methods for configuration parameters

    public int getNumberOfPreferredNeighbors() {
        return numberOfPreferredNeighbors;
    }

    public void setNumberOfPreferredNeighbors(int numberOfPreferredNeighbors) {
        this.numberOfPreferredNeighbors = numberOfPreferredNeighbors;
    }

    public int getUnchokingInterval() {
        return unchokingInterval;
    }

    public void setUnchokingInterval(int unchokingInterval) {
        this.unchokingInterval = unchokingInterval;
    }

    public int getOptimisticUnchokingInterval() {
        return optimisticUnchokingInterval;
    }

    public void setOptimisticUnchokingInterval(int optimisticUnchokingInterval) {
        this.optimisticUnchokingInterval = optimisticUnchokingInterval;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public int getFileSize() {
        return fileSize;
    }

    public void setFileSize(int fileSize) {
        this.fileSize = fileSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    /**
     * The main method for independent class testing.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        CommonConfigParser commonConfigInfo = new CommonConfigParser("Common.cfg");
        System.out.println(commonConfigInfo);
    }
}
