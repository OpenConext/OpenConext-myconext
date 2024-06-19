package myconext.manage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import lombok.SneakyThrows;
import myconext.model.IdentityProvider;
import myconext.model.ServiceProvider;
import org.junit.ClassRule;
import org.junit.Test;

import java.util.Optional;
import java.util.Set;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static myconext.AbstractIntegrationTest.readFile;
import static org.junit.Assert.*;

public class RemoteManageTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8999);

    private final Manage manage = new ManageConfiguration().manage("user", "secret", "http://localhost:8999", true, null);

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
    public void refresh() {
        stubForTokens("oidc10_rp");
        stubForTokens("saml20_sp");
        stubForTokens("saml20_idp");

        ((RemoteManage) this.manage).refresh();
    }

    @Test
    public void refreshWithoutErrors() {
        ((RemoteManage) this.manage).refresh();
    }

    @Test
    public void getDomainNames() {
        stubForTokens("saml20_idp");

        Set<String> domainNames = manage.getDomainNames();
        assertEquals(17, domainNames.size());
    }

    @Test
    public void findIdentityProviderByDomainName() {
        stubForTokens("saml20_idp");

        Optional<IdentityProvider> optionalIdentityProvider = manage.findIdentityProviderByDomainName("sub7.aap.nl");
        assertTrue(optionalIdentityProvider.isPresent());
    }

    @Test
    @SneakyThrows
    public void findIdentityProviderByBrinCode() {
        stubFor(post(urlPathMatching("/manage/api/internal/search/saml20_idp"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile("manage/idp_brin_code.json"))));
        IdentityProvider identityProvider = manage.findIdentityProviderByBrinCode("ST42").get();

        assertEquals("Hartingcollege ADFS IDP EN", identityProvider.getName());
    }

    @Test
    @SneakyThrows
    public void findIdentityProviderByUnknownBrinCode() {
        stubFor(post(urlPathMatching("/manage/api/internal/search/saml20_idp"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("[]")));
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