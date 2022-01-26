package myconext.manage;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import myconext.model.ServiceProvider;
import org.junit.ClassRule;
import org.junit.Test;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static myconext.AbstractIntegrationTest.readFile;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ManageServiceProviderResolverTest {

    @ClassRule
    public static WireMockRule wireMockRule = new WireMockRule(8999);

    private ServiceProviderResolver subject =
            new ManageServiceProviderResolver("user", "secret", "http://localhost:8999");

    @Test
    public void resolveLocally() throws IOException {
        stubForTokens("oidc10_rp");
        stubForTokens("saml20_sp");
        stubForTokens("oauth20_rs");

        ServiceProvider serviceProvider = subject.resolve("https://beta.surfnet.nl/SURFdropjesSP").get();

        assertEquals("SURFdrópjés test SP", serviceProvider.getNameNl());
        assertEquals("https://beta.surfnet.nl/simplesaml/module.php/core/authenticate.php?as=surfconextACC", serviceProvider.getHomeUrl());
        assertEquals("https://beta.surfnet.nl/coin/SURFdropsBeta.jpg", serviceProvider.getLogoUrl());
        assertEquals("5ede9c9b-3bbc-ea11-90fe-0050569571ea", serviceProvider.getInstitutionGuid());

        assertFalse(subject.resolve("bogus").isPresent());
    }

    private void stubForTokens(String metaDataType) throws IOException {
        stubFor(post(urlPathMatching("/manage/api/internal/search/" + metaDataType))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody(readFile(String.format("manage_%s.json", metaDataType)))));
    }


}