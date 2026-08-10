package myconext.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ACRTest {

    // ACR's fields are mutable public statics shared across the whole JVM (not per-test/per-context),
    // so any test that calls ACR.initialize(...) must restore the originals afterwards or it silently
    // corrupts every other test (in this fork) that relies on the real, config-driven ACR values.
    private String originalLinkedInstitution;
    private String originalValidateNames;
    private String originalValidateNamesExternal;
    private String originalAffiliationStudent;
    private String originalIapMedium;
    private String originalIapHigh;
    private String originalProfileMfa;
    private String originalLinkedInstitutionMfa;
    private String originalValidateNamesMfa;
    private String originalValidateNamesExternalMfa;
    private String originalAffiliationStudentMfa;
    private String originalIapMediumMfa;
    private String originalIapHighMfa;

    @BeforeEach
    void snapshotAcrStaticState() {
        originalLinkedInstitution = ACR.LINKED_INSTITUTION;
        originalValidateNames = ACR.VALIDATE_NAMES;
        originalValidateNamesExternal = ACR.VALIDATE_NAMES_EXTERNAL;
        originalAffiliationStudent = ACR.AFFILIATION_STUDENT;
        originalIapMedium = ACR.IAP_MEDIUM;
        originalIapHigh = ACR.IAP_HIGH;
        originalProfileMfa = ACR.PROFILE_MFA;
        originalLinkedInstitutionMfa = ACR.LINKED_INSTITUTION_MFA;
        originalValidateNamesMfa = ACR.VALIDATE_NAMES_MFA;
        originalValidateNamesExternalMfa = ACR.VALIDATE_NAMES_EXTERNAL_MFA;
        originalAffiliationStudentMfa = ACR.AFFILIATION_STUDENT_MFA;
        originalIapMediumMfa = ACR.IAP_MEDIUM_MFA;
        originalIapHighMfa = ACR.IAP_HIGH_MFA;
    }

    @AfterEach
    void restoreAcrStaticState() {
        ACR.initialize(
                originalLinkedInstitution,
                originalValidateNames,
                originalValidateNamesExternal,
                originalAffiliationStudent,
                originalIapMedium,
                originalIapHigh,
                originalProfileMfa,
                originalLinkedInstitutionMfa,
                originalValidateNamesMfa,
                originalValidateNamesExternalMfa,
                originalAffiliationStudentMfa,
                originalIapMediumMfa,
                originalIapHighMfa
        );
    }

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
    public void testContainsAcr_shouldReturnFalse_whenAcrValuesIsNull() {
        // Given
        List<String> acrValues = null;
        String acrValue = ACR.AFFILIATION_STUDENT;

        // When
        boolean result = ACR.containsAcr(acrValues, acrValue);

        // Then
        assertFalse(result);
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

    @Test
    public void testContainsMfaAcr_shouldReturnFalse_whenAcrValuesIsNull() {
        // Given
        List<String> acrValues = null;

        // When
        boolean result = ACR.containsMfaAcr(acrValues);

        // Then
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
    void testExplanationKeyWord_ValidateNamesExternalAndPresent() {
        List<String> acrValues = Collections.singletonList(ACR.VALIDATE_NAMES_EXTERNAL);

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("validate_names_external", result);
    }

    @Test
    void testExplanationKeyWord_ValidateNamesAndPresent() {
        List<String> acrValues = Collections.singletonList(ACR.VALIDATE_NAMES);

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("validate_names", result);
    }

    @Test
    void testExplanationKeyWord_RandomText() {
        List<String> acrValues = Collections.singletonList("unknown-acr");

        String result = ACR.explanationKeyWord(acrValues, true);

        assertEquals("linked_institution", result);
    }

    @Test
    public void testAllAccountLinkingContextClassReferences_shouldReturnAllAcrValues_whenCalled() {
        // When
        List<String> result = ACR.allAccountLinkingContextClassReferences();

        // Then;
        assertEquals(6, result.size());
        assertTrue(result.contains(ACR.VALIDATE_NAMES));
        assertTrue(result.contains(ACR.VALIDATE_NAMES_EXTERNAL));
        assertTrue(result.contains(ACR.LINKED_INSTITUTION));
        assertTrue(result.contains(ACR.AFFILIATION_STUDENT));
        assertTrue(result.contains(ACR.IAP_MEDIUM));
        assertTrue(result.contains(ACR.IAP_HIGH));
    }

    @Test
    public void testInitialize_shouldUpdateAllAcrValues_whenCalledWithNewValues() {
        // Given
        String newLinkedInstitution = "https://test.nl/linked-institution";
        String newValidateNames = "https://test.nl/validate-names";
        String newExternalValidateNames = "https://test.nl/validate-names-external";
        String newAffiliationStudent = "https://test.nl/affiliation-student";
        String newIapMedium = "https://test.nl/iap/medium";
        String newIapHigh = "https://test.nl/iap/high";
        String newProfileMfa = "https://refeds.org/profile/mfa";
        String newLinkedInstitutionMfa = "https://test.nl/linked-institution/mfa";
        String newValidateNamesMfa = "https://test.nl/validate-names/mfa";
        String newExternalValidateNamesMfa = "https://test.nl/validate-names-external/mfa";
        String newAffiliationStudentMfa = "https://test.nl/affiliation-student/mfa";
        String newIapMediumMfa = "https://test.nl/iap/medium/mfa";
        String newIapHighMfa = "https://test.nl/iap/high/mfa";

        // When
        ACR.initialize(
                newLinkedInstitution,
                newValidateNames,
                newExternalValidateNames,
                newAffiliationStudent,
                newIapMedium,
                newIapHigh,
                newProfileMfa,
                newLinkedInstitutionMfa,
                newValidateNamesMfa,
                newExternalValidateNamesMfa,
                newAffiliationStudentMfa,
                newIapMediumMfa,
                newIapHighMfa
                );

        // Then
        assertEquals(newLinkedInstitution, ACR.LINKED_INSTITUTION);
        assertEquals(newValidateNames, ACR.VALIDATE_NAMES);
        assertEquals(newExternalValidateNames, ACR.VALIDATE_NAMES_EXTERNAL);
        assertEquals(newAffiliationStudent, ACR.AFFILIATION_STUDENT);
        assertEquals(newIapMedium, ACR.IAP_MEDIUM);
        assertEquals(newIapHigh, ACR.IAP_HIGH);
        assertEquals(newProfileMfa, ACR.PROFILE_MFA);
        assertEquals(newLinkedInstitutionMfa, ACR.LINKED_INSTITUTION_MFA);
        assertEquals(newValidateNamesMfa, ACR.VALIDATE_NAMES_MFA);
        assertEquals(newExternalValidateNamesMfa, ACR.VALIDATE_NAMES_EXTERNAL_MFA);
        assertEquals(newAffiliationStudentMfa, ACR.AFFILIATION_STUDENT_MFA);
        assertEquals(newIapMediumMfa, ACR.IAP_MEDIUM_MFA);
        assertEquals(newIapHighMfa, ACR.IAP_HIGH_MFA);

    }
}

