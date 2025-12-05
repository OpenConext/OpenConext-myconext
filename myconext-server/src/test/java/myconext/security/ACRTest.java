package myconext.security;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    // containsAcr
    @Test
    void testContainsAcr() {
        List<String> acrValues = Arrays.asList(
            ACR.PROFILE_MFA,
            ACR.AFFILIATION_STUDENT
        );

        boolean result = ACR.containsAcr(acrValues, ACR.AFFILIATION_STUDENT);

        assertTrue(result);
    }

    @Test
    void testContainsAcr_Mfa() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT_MFA);

        boolean result = ACR.containsAcr(acrValues, ACR.AFFILIATION_STUDENT);

        assertTrue(result);
    }

    @Test
    void testContainsAcr_NotInList() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT_MFA);

        boolean result = ACR.containsAcr(acrValues, ACR.VALIDATE_NAMES);

        assertFalse(result);
    }

    @Test
    void testContainsAcr_EmptyList() {
        List<String> acrValues = Collections.emptyList();

        boolean result = ACR.containsAcr(acrValues, ACR.AFFILIATION_STUDENT);

        assertFalse(result);
    }

    @Test
    void testContainsAcr_NullValue() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT_MFA);

        boolean result = ACR.containsAcr(acrValues, null);

        assertFalse(result);
    }

    // containsAnyAcr
    @Test
    void testContainsAnyAcr() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT_MFA);
        List<String> targetAcrs = Arrays.asList(
            ACR.AFFILIATION_STUDENT,
            ACR.VALIDATE_NAMES_EXTERNAL
        );

        boolean result = ACR.containsAnyAcr(acrValues, targetAcrs);

        assertTrue(result);
    }

    @Test
    void testContainsAnyAcr_NoMatch() {
        List<String> acrValues = Collections.singletonList(ACR.AFFILIATION_STUDENT_MFA);
        List<String> targetAcrs = Collections.singletonList(ACR.VALIDATE_NAMES);

        boolean result = ACR.containsAnyAcr(acrValues, targetAcrs);

        assertFalse(result);
    }

    @Test
    void testContainsAnyAcr_AcrValuesNull() {
        List<String> acrValues = null;
        List<String> targetAcrs = Collections.singletonList(ACR.VALIDATE_NAMES);

        boolean result = ACR.containsAnyAcr(acrValues, targetAcrs);

        assertFalse(result);
    }

    @Test
    void testContainsAnyAcr_TargetAcrsNull() {
        List<String> acrValues = Collections.singletonList(ACR.VALIDATE_NAMES);
        List<String> targetAcrs = null;

        boolean result = ACR.containsAnyAcr(acrValues, targetAcrs);

        assertFalse(result);
    }

    // containsMfaAcr
    @Test
    void testContainsMfaAcr() {
        List<String> acrValues = Arrays.asList(
            ACR.AFFILIATION_STUDENT_MFA,
            ACR.VALIDATE_NAMES_MFA
        );

        boolean result = ACR.containsMfaAcr(acrValues);

        assertTrue(result);
    }

    @Test
    void testContainsMfaAcr_NoAcr() {
        List<String> acrValues = Arrays.asList(
                ACR.AFFILIATION_STUDENT,
                ACR.VALIDATE_NAMES
        );

        boolean result = ACR.containsMfaAcr(acrValues);

        assertFalse(result);
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

