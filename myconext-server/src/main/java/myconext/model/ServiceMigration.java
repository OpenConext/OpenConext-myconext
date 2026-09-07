package myconext.model;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
@AllArgsConstructor
public class ServiceMigration {

    @NotNull
    private String entityID;

    @NotNull
    private String institutionGUID;

    private boolean dryRun;

}
