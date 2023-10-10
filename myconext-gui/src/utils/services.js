import I18n from "i18n-js";

export const serviceName = service => {
  if (I18n.locale === "en") {
    return service.serviceName;
  }
  return service.serviceNameNl || service.serviceName;
}

export const institutionName = linkedAccount => {
  if (I18n.locale === "en") {
    return linkedAccount.displayNameEn || linkedAccount.displayNameNl || linkedAccount.schacHomeOrganization;
  }
  return linkedAccount.displayNameNl || linkedAccount.displayNameEn || linkedAccount.schacHomeOrganization;
}

