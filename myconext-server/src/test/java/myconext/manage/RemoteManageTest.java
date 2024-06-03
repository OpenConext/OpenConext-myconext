package myconext.manage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import myconext.model.IdentityProvider;
import myconext.model.ServiceProvider;
import org.junit.ClassRule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static myconext.AbstractIntegrationTest.readFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class RemoteManageTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8999);

    private final Manage manage =
            new RemoteManage("user", "secret", "http://localhost:8999");

    @Test
    public void findServiceProviderByEntityIdLocally() {
        stubForTokens("oidc10_rp");
        stubForTokens("saml20_sp");

        ServiceProvider serviceProvider = manage.findServiceProviderByEntityId("https://beta.surfnet.nl/SURFdropjesSP").get();

        assertEquals("SURFdrópjés test SP", serviceProvider.getNameNl());
        assertEquals("https://beta.surfnet.nl/simplesaml/module.php/core/authenticate.php?as=surfconextACC", serviceProvider.getHomeUrl());
        assertEquals("https://beta.surfnet.nl/coin/SURFdropsBeta.jpg", serviceProvider.getLogoUrl());
        assertEquals("5ede9c9b-3bbc-ea11-90fe-0050569571ea", serviceProvider.getInstitutionGuid());

        assertFalse(manage.findServiceProviderByEntityId("bogus").isPresent());
    }

    @Test
    public void findIdentityProviderByBrinCode() {
        stubForTokens("saml20_idp");

        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode("QW12").get();

        assertEquals("thkidp EN", identityProvider.getDisplayNameEn());

        assertFalse(manage.findIdentityProviderByBrinCode("nope").isPresent());
    }

    @SneakyThrows
    private void stubForTokens(String metaDataType) {
        stubFor(post(urlPathMatching("/manage/api/internal/search/" + metaDataType))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile(String.format("manage/%s.json", metaDataType)))));
    }


}