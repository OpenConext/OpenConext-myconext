package myconext.remotecreation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import myconext.security.RemoteUser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static myconext.SwaggerOpenIdConfig.BASIC_AUTHENTICATION_SCHEME_NAME;


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
    public ResponseEntity<Map<String, Object>> remoteCreation(@Parameter(hidden = true) @AuthenticationPrincipal RemoteUser remoteUser) {
        LOG.debug(String.format("Remote creation for %s", remoteUser.getInstitutionGUID()));

        return ResponseEntity.ok(Map.of("status", "todo"));
    }

    //TODO
    /*
    curl -H 'Content-Type: application/json' -u pdp:secret  -X POST -d
    '{"metaDataFields.coin:institution_guid":"ad93daef-0911-e511-80d0-005056956c1a","REQUESTED_ATTRIBUTES":["metaDataFields.coin:institution_brin"]}' 'https://manage.test2.surfconext.nl/manage/api/internal/search/saml20_idp'
     */

}
