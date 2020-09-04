package myconext.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@Getter
public class EduID {

    private String value;
    private String serviceName;
    private String serviceNameNl;
    private Date createdAt;

    public void updateServiceName(String serviceName, String serviceNameNl) {
        this.serviceName = serviceName;
        this.serviceNameNl = serviceNameNl;
    }
}
