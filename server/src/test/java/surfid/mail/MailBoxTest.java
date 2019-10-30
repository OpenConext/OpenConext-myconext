package surfid.mail;

import com.icegreen.greenmail.junit.GreenMailRule;
import com.icegreen.greenmail.store.FolderException;
import com.icegreen.greenmail.util.ServerSetupTest;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import teams.AbstractApplicationTest;
import teams.domain.*;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.IOException;

import static com.icegreen.greenmail.util.GreenMailUtil.getBody;
import static java.util.Collections.singletonList;
import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, value = {"spring.profiles.active=prod"})
public class MailBoxTest extends AbstractApplicationTest {

    private static final String EMAIL = "test@test.org";
    private static final String TEAM_DESCRIPTION = "name";

    @Autowired
    private MailBox mailBox;

    @Rule
    public final GreenMailRule greenMail = new GreenMailRule(ServerSetupTest.SMTP);

    @Before
    public void before() throws FolderException {
        greenMail.purgeEmailFromAllMailboxes();
    }

    @Test
    public void sendInviteMail() throws Exception {
        Team team = new Team("urn", "Champions", "description", true, null);
        Person person = new Person("urn", "John Doe", EMAIL, false);

        Invitation invitation = new Invitation(team, EMAIL, Role.ADMIN, Language.DUTCH, null);
        invitation.addInvitationMessage(person, "Please join");

        mailBox.sendInviteMail(invitation, federatedUser());

        String body = mailBody();
        assertTrue(body.contains("Uitnodiging voor Champions"));
        assertTrue(body.contains("<strong>Persoonlijk bericht van John Doe:</strong><br>\"Please join\""));
        assertTrue(body.contains("<span><a href=\"http://localhost:8001/invitation/accept/"));
    }

    @Test
    public void sendInviteMailPortugese() throws Exception {
        Team team = new Team("urn", "Champions", "description", true, null);
        Person person = new Person("urn", "John Doe", EMAIL, false);

        Invitation invitation = new Invitation(team, EMAIL, Role.ADMIN, Language.PORTUGUESE, null);
        invitation.addInvitationMessage(person, "Please join");

        mailBox.sendInviteMail(invitation, federatedUser());

        String body = mailBody();
        assertTrue(body.contains("Convite para Champions"));
    }

    @Test
    public void sendJoinRequestMail() throws Exception {
        JoinRequest joinRequest = joinRequest();
        mailBox.sendJoinRequestMail(joinRequest, singletonList(EMAIL), federatedUser());

        String body = mailBody();
        assertTrue(body.contains(String.format("\"%s\"", joinRequest.getTeam().getHtmlDescription())));
        assertTrue(body.contains(String.format("\"%s\"", joinRequest.getHtmlMessage())));
        assertTrue(body.contains(String.format("<a href=\"mailto:%s\">%s</a> would like to join team",
                EMAIL, joinRequest.getPerson().getName())));
    }

    @Test
    public void sendJoinRequestAcceptedMail() throws Exception {
        mailBox.sendJoinRequestAccepted(joinRequest(), federatedUser());
        validateJoinRequestStatusMail("accepted");
    }

    @Test
    public void sendJoinRequestDeclinedMail() throws Exception {
        mailBox.sendJoinRequestRejected(joinRequest(), federatedUser());
        validateJoinRequestStatusMail("declined");
    }

    private void validateJoinRequestStatusMail(String status) throws IOException, MessagingException, InterruptedException {
        String mailBody = mailBody();
        assertTrue(mailBody.contains(String.format("has been <strong>%s</strong>", status)));
        assertTrue(mailBody.contains("http://localhost:8001/teams/"));
    }

    private String mailBody() throws InterruptedException, MessagingException {
        await().until(() -> greenMail.getReceivedMessages().length != 0);
        MimeMessage mimeMessage = greenMail.getReceivedMessages()[0];
        assertEquals(EMAIL, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        return getBody(mimeMessage);
    }

    private JoinRequest joinRequest() {
        Person person = new Person("urn", "John Doe", EMAIL, false);
        Team team = new Team("urn", "name", TEAM_DESCRIPTION, true, null);
        return new JoinRequest(person, team, "Let me join");
    }
}