import I18n from "i18n-js";

export const serviceName = service => {
  if (I18n.locale === "en") {
    return service.serviceName;
  }
  return service.serviceNameNl || service.serviceName;
}
