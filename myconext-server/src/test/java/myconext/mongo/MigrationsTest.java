package myconext.mongo;

import myconext.model.EduID;
import myconext.model.User;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

public class MigrationsTest {


    @Test
    public void bugfixForDotReplacement() {
        User user = new User();
        user.setEduIDS(Arrays.asList(
                eduID("brand.new"),
                eduID("demo2.edubadges.nl"),
                new EduID("existing-eduid", "demo2@edubadges@nl", Optional.empty()),
                eduID("playground")
        ));
        Migrations migrations = new Migrations();
        user = migrations.mergeEduIDs(user);
        List<EduID> eduIDS = user.getEduIDS();

        assertEquals(3, eduIDS.size());
        assertEquals("existing-eduid", eduIDS.stream().filter(eduID -> eduID.getServiceProviderEntityId().equals("demo2.edubadges.nl")).findFirst().get().getValue());
    }

    private EduID eduID(String entityId) {
        return new EduID(UUID.randomUUID().toString(), entityId, Optional.empty());
    }
}