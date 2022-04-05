package myconext.api;

import com.google.zxing.WriterException;
import myconext.repository.AuthenticationRepository;
import myconext.repository.EnrollmentRepository;
import myconext.repository.RegistrationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tiqr.org.TiqrService;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Service;
import tiqr.org.secure.QRCodeGenerator;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/tiqrenroll")
public class TiqrController {

    private final TiqrService tiqrService;

    @Autowired
    public TiqrController(EnrollmentRepository enrollmentRepository,
                          RegistrationRepository registrationRepository,
                          AuthenticationRepository authenticationRepository) {
        //TODO move to application yml
        Service service = new Service(
                "displayName",
                "identifier",
                "http://localhost:8080/tiqr/logo",
                "http://localhost:8080/tiqr/info",
                "http://localhost:8080/tiqr/authentication",
                "http://localhost:8080/tiqr/enrollment");
        this.tiqrService = new TiqrService(enrollmentRepository, registrationRepository, authenticationRepository, service, "secret");
    }

    @GetMapping("/test")
    ResponseEntity<Map<String, String>> start() throws IOException, WriterException {
        Enrollment enrollment = tiqrService.startEnrollment("user-id", "John Doe");
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollment.getKey(),
                "qr", QRCodeGenerator.generateQRCodeImage("http://localhost:8080/tiqrenroll/metadata?" + enrollment.getKey())
        );
        return ResponseEntity.ok(results);
    }

//    @GetMapping("/metadata")
//    MetaData metaData(@Param("enrollment_key") String enrollmentKey) {
//        tiqrService.getMetaData(enrollmentKey);
//    }
}
