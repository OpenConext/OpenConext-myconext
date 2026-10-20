package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;

/**
 * A user action, stored in the {@link User#getActivities()} of the user document.
 */
@Getter
@NoArgsConstructor
public class Activity implements Serializable {

    private String userId;
    private ActivityEvent event;
    @Schema(type = "integer", format = "int64", example = "1634813554997")
    private Date dateTime;
    private String description;

    public Activity(String userId, ActivityEvent event, Date dateTime, String description) {
        this.userId = userId;
        this.event = event;
        this.dateTime = dateTime;
        this.description = description;
    }
}
