package myconext.api;

import com.google.zxing.WriterException;
import myconext.repository.AuthenticationRepository;
import myconext.repository.EnrollmentRepository;
import myconext.repository.RegistrationRepository;
import myconext.tiqr.TiqrInitializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tiqr.org.TiqrService;
import tiqr.org.model.Enrollment;
import tiqr.org.model.Service;
import tiqr.org.secure.QRCodeGenerator;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/tiqr")
public class TiqrController {

    private final TiqrService service;
    private final TiqrInitializer initializer;

    @Autowired
    public TiqrController(TiqrInitializer initializer,
                          EnrollmentRepository enrollmentRepository,
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
        this.service = new TiqrService(enrollmentRepository, registrationRepository, authenticationRepository, service, "secret");
        this.initializer = initializer;
    }

    @GetMapping("/test")
    ResponseEntity<Map<String, String>> start() throws IOException, WriterException {
        Enrollment enrollment = service.startEnrollment("user-id", "John Doe");
        Map<String, String> results = Map.of(
                "enrollmentKey", enrollment.getKey(),
                "qr", QRCodeGenerator.generateQRCodeImage("http://localhost:8080/tiqrenroll/metadata?" + enrollment.getKey())
        );
        return ResponseEntity.ok(results);
    }

    @PostMapping("/qrcode")
    ResponseEntity<Map<String, String>> qrcode(@RequestBody Map<String, String> body) throws IOException, WriterException {
        Map<String, String> result = Collections.singletonMap("qrcode", QRCodeGenerator.generateQRCodeImage(body.get("url")));
        return ResponseEntity.ok(result);
    }

//    @GetMapping("/metadata")
//    MetaData metaData(@Param("enrollment_key") String enrollmentKey) {
//        tiqrService.getMetaData(enrollmentKey);
//    }
}
