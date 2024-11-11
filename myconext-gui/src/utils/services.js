import I18n from "i18n-js";
import {logo} from "../verify/banks";
import {isEmpty} from "./utils";
import notFound from "../icons/school-building.svg";

export const serviceName = service => {
  //backward compatibility with eduID has multiple services
  if (I18n.locale === "en") {
    return service.serviceName || service.name;
  }
  return service.serviceNameNl || service.serviceName || service.nameNl || service.name;
}

export const institutionLogo = institution => {
  if (institution.external) {
    return logo(institution.issuer)
  }
  if (!isEmpty(institution.LogoUrl)) {
    return institution.LogoUrl;
  }
  return notFound;

}

export const institutionName = linkedAccount => {
  if (linkedAccount.external) {
    return I18n.t(`verify.issuers.${linkedAccount.issuer.name}`);
  }
  if (I18n.locale === "en") {
    return linkedAccount.displayNameEn || linkedAccount.displayNameNl || linkedAccount.schacHomeOrganization;
  }
  return linkedAccount.displayNameNl || linkedAccount.displayNameEn || linkedAccount.schacHomeOrganization;
}

export const affiliation = linkedAccount => {
  if (linkedAccount.external || isEmpty(linkedAccount.eduPersonAffiliations)) {
    return null;
  }
  const eduPersonAffiliation = linkedAccount.eduPersonAffiliations[0];
  return eduPersonAffiliation.substring(0, eduPersonAffiliation.indexOf("@"))
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
