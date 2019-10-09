package surfid.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/surfid/api")
public class UserController {

    @GetMapping("/user")
    public Map<String, String> user() {
        return Collections.singletonMap("name", "John Doe");
    }

}
