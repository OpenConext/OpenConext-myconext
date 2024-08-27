package myconext.mongo;

import myconext.model.User;
import org.bson.Document;
import org.springframework.data.mongodb.core.mapping.event.BeforeSaveCallback;
import org.springframework.stereotype.Component;

@Component
public class UserBeforeSaveCallBack implements BeforeSaveCallback<User> {

    @Override
    public User onBeforeSave(User entity, Document document, String collection) {
        //We don't want leading or trailing spaces in names
        String chosenName = entity.getChosenName();
        if (chosenName != null) {
            entity.setChosenName(chosenName.trim());
        }
        String familyName = entity.getFamilyName();
        if (familyName != null) {
            entity.setFamilyName(familyName.trim());
        }
        String givenName = entity.getGivenName();
        if (givenName != null) {
            entity.setGivenName(givenName.trim());
        }
        return entity;
    }
}
