package myconext.model;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;

public class EduIDTest {

    @Test
    public void fromMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("value", "1234567890");
        map.put("serviceName", "SP");
        map.put("createdAt", "2020-11-12T13:55:50.229Z");

        EduID eduID = new EduID("entityId", map);
        assertNotNull(eduID.getCreatedAt().toString());

    }

}