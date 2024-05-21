package myconext.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import myconext.model.IdinIssuers;
import myconext.model.VerifyIssuer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = {"/myconext/api", "/mobile/api"})
public class VerifyController {

    private static final Log LOG = LogFactory.getLog(VerifyController.class);

    private final List<VerifyIssuer> issuers;

    //For now, hardcode the not known issuers from test
    private final List<String> unknownIssuers = List.of("CURRNL2A");

    @Autowired
    public VerifyController(@Value("${verify.issuers_path}") Resource issuersResource, ObjectMapper objectMapper) throws IOException {
        List<IdinIssuers> idinIssuers = objectMapper.readValue(issuersResource.getInputStream(), new TypeReference<>() {
        });
        //For now, we only support "Nederland"
        this.issuers = idinIssuers.get(0).getIssuers().stream().filter(issuer -> !unknownIssuers.contains(issuer.getId())).collect(Collectors.toList());
        LOG.debug(String.format("Initialized IDIN issuers %s from %s", this.issuers, issuersResource.getDescription()));
    }

    @GetMapping("/sp/idin/issuers")
    public ResponseEntity<List<VerifyIssuer>> issuers() {
        LOG.debug("Retrieve IDIN issuers");
        return ResponseEntity.ok(issuers);
    }
}
