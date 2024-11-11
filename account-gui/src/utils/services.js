import I18n from "i18n-js";

export const institutionName = linkedAccount => {
  if (linkedAccount.external) {
    const translation = I18n.translations[I18n.locale];
    if (translation.verify.issuers[linkedAccount.issuer.name]) {
      return I18n.t(`verify.issuers.${linkedAccount.issuer.name}`);
    }
    return linkedAccount.issuer.name;
  }
  if (I18n.locale === "en") {
    return linkedAccount.displayNameEn || linkedAccount.displayNameNl || linkedAccount.schacHomeOrganization;
  }
  return linkedAccount.displayNameNl || linkedAccount.displayNameEn || linkedAccount.schacHomeOrganization;
}

export const linkedAccountGivenName = linkedAccount => {
  return linkedAccount.external ? linkedAccount.firstName : linkedAccount.givenName;
}

export const linkedAccountFamilyName = linkedAccount => {
  return linkedAccount.external ? linkedAccount.legalLastName : linkedAccount.familyName;
}

export const isStudent = linkedAccount => {
  return (linkedAccount.eduPersonAffiliations || []).some(aff => aff.toLowerCase().indexOf("student") > -1);
}
