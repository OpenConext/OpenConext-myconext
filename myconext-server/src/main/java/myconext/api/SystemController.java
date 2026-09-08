package myconext.api;

import io.swagger.v3.oas.annotations.Hidden;
import jakarta.validation.Valid;
import lombok.Getter;
import myconext.aa.UserAttribute;
import myconext.exceptions.ServiceProviderNotFoundException;
import myconext.manage.Manage;
import myconext.model.EduID;
import myconext.model.RemoteProvider;
import myconext.model.ServiceMigration;
import myconext.model.ServiceProvider;
import myconext.model.User;
import myconext.repository.UserRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;


@RestController
@RequestMapping("/myconext/api")
@Hidden
public class SystemController {

    private static final Log LOG = LogFactory.getLog(SystemController.class);

    private final UserRepository userRepository;
    private final Manage manage;

    public SystemController(UserRepository userRepository, Manage manage) {
        this.userRepository = userRepository;
        this.manage = manage;
    }

    @GetMapping(value = "system/eduid-duplicates")
    @PreAuthorize("hasRole('ROLE_system')")
    public ResponseEntity<Map<String, List<EduID>>> eduIdDuplicates(Authentication authentication) {

        LOG.info("system/eduid-duplicates called by " + authentication.getName());

        List<EduID> eduIDs = userRepository.findAll().stream()
                .map(User::getEduIDS)
                .flatMap(Collection::stream)
                .filter(eduID -> StringUtils.hasText(eduID.getServiceInstutionGuid()))
                .toList();
        Set<EduID> uniqueSet = new TreeSet<>(Comparator.comparing(EduID::getServiceProviderEntityId));
        uniqueSet.addAll(eduIDs);
        Map<String, List<EduID>> eduIdValuesGroupedBy = uniqueSet.stream()
                .collect(Collectors.groupingBy(EduID::getServiceInstutionGuid));
        eduIdValuesGroupedBy.values().removeIf(l -> l.size() < 2);
        return ResponseEntity.ok(eduIdValuesGroupedBy);
    }

    @PostMapping(value = "system/service-migration")
    @PreAuthorize("hasRole('ROLE_system')")
    public ResponseEntity<List<Map<String, String>>> serviceMigration(
            @Valid @RequestBody ServiceMigration serviceMigration, Authentication authentication) {
        LOG.info("system/service-migration called by " + authentication.getName() + " with body " + serviceMigration);

        String institutionGUID = serviceMigration.getInstitutionGUID();
        String entityID = serviceMigration.getEntityID();
        boolean dryRun = serviceMigration.isDryRun();

        ServiceProvider serviceProvider = manage.findServiceProviderByEntityId(entityID)
                .orElseThrow(() -> new ServiceProviderNotFoundException("Service not found in Manage: " + entityID));
        serviceProvider.setInstitutionGuid(institutionGUID);

        List<User> users = userRepository.findByEduIDS_Services_EntityId(entityID);
        List<Map<String, String>> results = new ArrayList<>();
        users.forEach(user -> {
            //Find the eduID that actually holds the service being migrated, regardless of its current institutionGUID
            //There must be one, because the query `findByEduIDS_Services_EntityId`, but let's stick to conventions
            Optional<EduID> optionalSourceEduID = user.getEduIDS().stream()
                    .filter(eduID -> eduID.getServices().stream()
                            .anyMatch(service -> entityID.equals(service.getEntityId())))
                    .findAny();
            optionalSourceEduID.ifPresent(sourceEduID -> {
                boolean hasOtherEduIDForNewInstitution = user.getEduIDS().stream()
                        .anyMatch(eduID -> eduID != sourceEduID && institutionGUID.equals(eduID.getServiceInstutionGuid()));
                if (!hasOtherEduIDForNewInstitution) {
                    //The user has not used any other service for the new institutionGUID, just update the institutionGUID for this service
                    sourceEduID.setServiceInstutionGuid(institutionGUID);
                    sourceEduID.getServices().stream()
                            .filter(service -> entityID.equals(service.getEntityId()))
                            .forEach(service -> service.setInstitutionGuid(institutionGUID));
                } else {
                    //The user has already used another service with the given institutionGUID, move the service to that eduID identifier
                    sourceEduID.getServices().removeIf(service -> entityID.equals(service.getEntityId()));
                    if (sourceEduID.getServices().isEmpty()) {
                        user.getEduIDS().remove(sourceEduID);
                    }
                    if (dryRun) {
                        results.add(Map.of("user", user.getEmail(), "uid", user.getUid(), "currentEduID", sourceEduID.getValue()));
                    } else {
                        String newValue = user.doComputeEduIDIfAbsent(serviceProvider, manage, false);
                        results.add(Map.of("user", user.getEmail(), "uid", user.getUid(), "oldEduID", sourceEduID.getValue(), "newEduID", newValue));
                    }
                }
                if (!dryRun) {
                    userRepository.save(user);
                }
            });
        });
        return ResponseEntity.ok(results);
    }

}
