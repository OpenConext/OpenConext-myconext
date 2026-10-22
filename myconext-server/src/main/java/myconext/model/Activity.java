package myconext.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
    @Schema(description = "Language-neutral details of the event, the values for the placeholders in the translation of the event")
    private Map<String, String> context = new LinkedHashMap<>();

    public Activity(String userId, ActivityEvent event, Date dateTime, Map<String, String> context) {
        this.userId = userId;
        this.event = event;
        this.dateTime = dateTime;
        this.context = context == null ? new LinkedHashMap<>() : new LinkedHashMap<>(context);
    }

    public Map<String, String> getContext() {
        return context == null ? Map.of() : context;
    }
}
