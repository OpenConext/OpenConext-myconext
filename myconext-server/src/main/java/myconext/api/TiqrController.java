package myconext.api;

import eduid.TiqrService;
import eduid.model.MetaData;
import org.springframework.data.repository.query.Param;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/tigrenroll")
public class TiqrController {

   // private TiqrService tiqrService = new TiqrService();

    @GetMapping("/enrollment")
    void start(@Param("enrollment_key") String enrollmentKey) {
       // return tiqrService.startEnrollment();
    }

    @GetMapping("/metadata")
    MetaData metaData(@Param("enrollment_key") String enrollmentKey) {
        return null;//tiqrService.getMetaData(enrollmentKey);
    }
}
