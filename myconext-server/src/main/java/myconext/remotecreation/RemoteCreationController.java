package myconext.remotecreation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import myconext.security.RemoteUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static myconext.SwaggerOpenIdConfig.BASIC_AUTHENTICATION_SCHEME_NAME;
import static myconext.SwaggerOpenIdConfig.OPEN_ID_SCHEME_NAME;


@RestController
@RequestMapping(value = {"/api/remote-creation"})
@SecurityRequirement(name = BASIC_AUTHENTICATION_SCHEME_NAME)
public class RemoteCreationController {

    private static final Log LOG = LogFactory.getLog(RemoteCreationController.class);

    public RemoteCreationController() {
    }

    @GetMapping(value = {"/eduid-exists"})
    @PreAuthorize("hasRole('ROLE_remote-creation')")
    @Operation(summary = "Does the eduID exists",
            description = "Does the eduID exists")
    public ResponseEntity<Map<String, Object>> remoteCreation(Authentication authentication) {
        RemoteUser remoteUser = (RemoteUser) authentication.getPrincipal();

        LOG.debug(String.format("Remote creation for %s", remoteUser.getRpClientId()));

        return ResponseEntity.ok(Map.of("status", "todo"));
    }

}
