package myconext.model;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class AllowedDomainTest {

    @Test
    public void toLowerCase() {
        AllowedDomain rug = new AllowedDomain();
        rug.setEmailDomain("rug.nl");
        rug.setSchacHomeOrganization("rug");
        rug = rug.toLowerCase();

        AllowedDomain alias = new AllowedDomain();
        alias.setEmailDomain("RUG.NL");
        alias.setSchacHomeOrganization("alias");
        alias = alias.toLowerCase();

        assertTrue(rug.toLowerCase().equals(alias.toLowerCase()));
    }
}