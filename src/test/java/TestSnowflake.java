import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class TestSnowflake {
    /**
     * Generate 1 million Snowflake IDs and check that no duplicates are generated
     */
    @Test
    public void snowflakeTest() {
        Snowflake snowflake = new Snowflake(5, 5);

        Map<Long, Integer> idMap = new HashMap<>();
        int count = 1000000;

        for (int i = 0; i < count; i++) {
            long id = snowflake.nextID();

            if (idMap.containsKey(id)) {
                System.out.println("Duplicate found ID: " + id);
                return;
            }
            Assertions.assertFalse(idMap.containsKey(id));
            idMap.put(id, i);
        }
        System.out.println("All " + idMap.size() + " Snowflake IDs generated successfully.");
    }
}
