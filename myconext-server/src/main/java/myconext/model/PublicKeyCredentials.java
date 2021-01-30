package myconext.model;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Getter
@NoArgsConstructor
public class PublicKeyCredentials {

    private String identifier;
    private String credential;
    private String name;
    private Date createdAt;

    public PublicKeyCredentials(PublicKeyCredentialDescriptor publicKeyCredentialDescriptor, ByteArray publicKeyCredential, String name) {
        this(publicKeyCredentialDescriptor.getId().getBase64Url(), publicKeyCredential.getBase64Url(), name);
    }

    public PublicKeyCredentials(String identifier, String credential, String name) {
        this.identifier = identifier;
        this.credential = credential;
        this.name = name;
        this.createdAt = new Date();
    }

    public void setName(String name) {
        this.name = name;
    }
}
