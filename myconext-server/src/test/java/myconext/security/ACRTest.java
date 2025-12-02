package myconext.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ACRTest {
    // selectACR
    @Test
    void testSelectACR_ProfileMfaHasHighestPriority() {
        List<String> acrValues = Arrays.asList(
                ACR.LINKED_INSTITUTION,
                ACR.VALIDATE_NAMES,
                ACR.PROFILE_MFA,
                ACR.AFFILIATION_STUDENT
        );

        String result = ACR.selectACR(acrValues, true);

        assertEquals(ACR.PROFILE_MFA, result);
    }

    @Test
    void testSelectACR_AffiliationStudentAndPresent() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT);

        String result = ACR.selectACR(acrValues, true);

        assertEquals(ACR.AFFILIATION_STUDENT, result);
    }

    @Test
    void testSelectACR_AffiliationStudentAndNotPresent() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT);

        String result = ACR.selectACR(acrValues, false);

        assertEquals(ACR.LINKED_INSTITUTION, result);
    }

    @Test
    void testSelectACR_PrioMfaOverNonMfa() {
        List<String> acrValues = Arrays.asList(
                ACR.VALIDATE_NAMES,
                ACR.VALIDATE_NAMES_MFA
        );

        String result = ACR.selectACR(acrValues, false);

        assertEquals(ACR.VALIDATE_NAMES_MFA, result);
    }

    @Test
    void testSelectACR_PrioSpecificMfaOverGenericMfa() {
        List<String> acrValues = Arrays.asList(
                ACR.PROFILE_MFA,
                ACR.VALIDATE_NAMES_MFA
        );

        String result = ACR.selectACR(acrValues, false);

        assertEquals(ACR.VALIDATE_NAMES_MFA, result);
    }

    // explanationKeyWord
    @Test
    void testExplanationKeyWord_EmptyList() {
        List<String> acrValues = Collections.emptyList();

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("linked_institution", result);
    }

    @Test
    void testExplanationKeyWord_LinkedInstitution() {
        List<String> acrValues = Collections.singletonList(ACR.LINKED_INSTITUTION);

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("linked_institution", result);
    }

    @Test
    void testExplanationKeyWord_AffiliationStudentAndPresent() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT);

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("affiliation_student", result);
    }

    @Test
    void testExplanationKeyWord_AffiliationStudentAndNotPresent() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT);

        String result = ACR.explanationKeyWord(acrValues, false);

        assertEquals("linked_institution", result);
    }

    @Test
    void testExplanationKeyWord_RandomText() {
        List<String> acrValues = Collections.singletonList("unknown-acr");

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("linked_institution", result);
    }
}

