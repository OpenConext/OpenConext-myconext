import I18n from "i18n-js";

export const serviceName = service => {
  //backward compatibility with eduID has multiple services
  if (I18n.locale === "en") {
    return service.serviceName || service.name;
  }
  return service.serviceNameNl || service.serviceName || service.nameNl || service.name;
}

export const institutionName = linkedAccount => {
  if (linkedAccount.external) {
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
