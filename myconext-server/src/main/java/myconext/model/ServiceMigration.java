package myconext.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
@ToString
public class ServiceMigration {

    @NotNull
    private String entityID;

    @NotNull
    private String institutionGUID;

    private boolean dryRun;

}
