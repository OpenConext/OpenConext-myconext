package myconext.security;

public class EmailGuessingPrevention {

    private int millis;

    public EmailGuessingPrevention(int millis) {
        this.millis = millis;
    }

    public void potentialUserEmailGuess() {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            //don't care
        }
    }

    public String sanitizeEmail(String email) {
        return email != null ? email.trim() : email;
    }
}
