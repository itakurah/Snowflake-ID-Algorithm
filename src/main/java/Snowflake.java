import java.util.HashMap;
import java.util.Map;

public class Snowflake {
    // Define basic constants
    // 5bit workerID out of 10bit worker machine ID
    private static final int workerIDBits = 5;
    // 5bit workerID out of 10bit worker dataCenterID
    private static final int dataCenterIDBits = 5;
    private static final int sequenceBits = 12;
    // The maximum value of the node ID used to prevent overflow
    private static final long maxWorkerID = -1L ^ (-1L << workerIDBits);
    private static final long maxDataCenterID = -1L ^ (-1L << dataCenterIDBits);
    private static final long maxSequence = -1L ^ (-1L << sequenceBits);
    // timeLeft = workerIDBits + sequenceBits // Timestamp offset left
    private static final int timeLeft = 22;
    // dataLeft = dataCenterIDBits + sequenceBits
    private static final int dataLeft = 17;
    // workLeft = sequenceBits // Node IDx offset to the left
    private static final int workLeft = 12;
    private static final long defaultEpochTimestamp = 1288834974000L; // constant timestamp (milliseconds) Nov 04, 2010, 01:42:54 UTC
    private final long workerID;
    private final long dataCenterID;
    private long sequence = 0L;
    private long lastTimestamp = -1L;

    public Snowflake(long workerID, long dataCenterID) {
        if (workerID < 0 || workerID > maxWorkerID) {
            throw new IllegalArgumentException("Worker ID can't be greater than " + maxWorkerID + " or less than 0");
        }
        if (dataCenterID < 0 || dataCenterID > maxDataCenterID) {
            throw new IllegalArgumentException("DataCenter ID can't be greater than " + maxDataCenterID + " or less than 0");
        }
        this.workerID = workerID;
        this.dataCenterID = dataCenterID;
    }

    private long getCurrentTimestamp() {
        return System.currentTimeMillis();
    }

    public synchronized long nextID() {
        long timestamp = getCurrentTimestamp();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException("Clock moved backward, waiting until " + lastTimestamp);
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & maxSequence;
            if (sequence == 0) {
                // If we have exhausted the sequence numbers for the current millisecond, wait until the next millisecond.
                timestamp = waitUntilNextMillisecond(lastTimestamp);
            }
        } else {
            sequence = 0;
        }

        lastTimestamp = timestamp;

        return ((timestamp - defaultEpochTimestamp) << timeLeft) | (dataCenterID << dataLeft) | (workerID << workLeft) | sequence;
    }

    private long waitUntilNextMillisecond(long lastTimestamp) {
        long timestamp = getCurrentTimestamp();
        while (timestamp <= lastTimestamp) {
            timestamp = getCurrentTimestamp();
        }
        return timestamp;
    }

    public static void main(String[] args) {
        Snowflake snowflake = new Snowflake(5, 5);

        Map<Long, Integer> idMap = new HashMap<>();
        int count = 1000000;

        for (int i = 0; i < count; i++) {
            long id = snowflake.nextID();

            if (idMap.containsKey(id)) {
                System.out.println("Repeat ID: " + id);
                return;
            }
            System.out.println(id);

            idMap.put(id, i);
        }

        System.out.println("All " + idMap.size() + " Snowflake IDs generated successfully.");

    }
}
