package surfid;

import org.junit.Test;

public class SurfIdServerApplicationTest {

    @Test
    public void main() {
        SurfIdServerApplication.main(new String[]{"--server.port=8088"});
    }
}