<script>
    import I18n from "../locale/I18n";
    import {onMount} from 'svelte';
    import {conf, links} from "../stores/conf";
    import Button from "../components/Button.svelte";
    import Cookies from "js-cookie";
    import {cookieNames} from "../constants/cookieNames";
    import phone from "../icons/redesign/eduIDapp.svg?raw";
    import ButtonContainer from "../components/ButtonContainer.svelte";
    import ImageContainer from "../components/ImageContainer.svelte";
    import {navigate} from "svelte-routing";
    import {proceed} from "../utils/sso";
    import {user} from "../stores/user";
    import DOMPurify from "dompurify";

    let hash = null;

    onMount(() => {
        $links.displayBackArrow = false;
        const urlSearchParams = new URLSearchParams(window.location.search);
        hash = urlSearchParams.get("h");
        const email = urlSearchParams.get("email");
        if (email) {
            const decodedEmail = DOMPurify.sanitize(decodeURIComponent(email));
            $user.knownUser = decodedEmail;
            Cookies.set(cookieNames.USERNAME, decodedEmail, {
                expires: 365,
                secure: true,
                sameSite: "Lax"
            });
        }
    });

</script>

<style>

    h2 {
        color: var(--color-primary-green);
    }

    p.explanation {
        margin: 15px 0;
    }

</style>

<h2 class="header">{I18n.t("Security.Tiqr.Title.COPY")}</h2>
<ImageContainer icon={phone} margin={false}/>
<p class="explanation">{@html I18n.t("NudgeApp.Info.COPY")}</p>
<ButtonContainer>
    <Button className="cancel" href={I18n.t("NudgeApp.NoLink.COPY")} onClick={() => proceed($conf.magicLinkUrl)}
            label={I18n.t("NudgeApp.No.COPY")}/>
    <Button href={I18n.t("NudgeApp.YesLink.COPY")} onClick={() => navigate(`/getapp?h=${hash}`)}
            label={I18n.t("Security.Tiqr.Fetch.COPY")}/>
</ButtonContainer>
