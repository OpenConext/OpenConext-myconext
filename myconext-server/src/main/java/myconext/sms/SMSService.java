package myconext.sms;

import java.util.Locale;

public interface SMSService {

    String send(String mobile, String code, Locale locale);
}
