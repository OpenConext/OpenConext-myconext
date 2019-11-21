package myconext;

import org.junit.Test;

public class SurfIdServerApplicationTest {

    @Test
    public void main() {
        MyConextServerApplication.main(new String[]{"--server.port=8088"});
    }
}