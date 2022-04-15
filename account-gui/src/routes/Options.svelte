<script>
    import {user} from "../stores/user";
    import I18n from "i18n-js";
    import {knownAccount} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import appIcon from "../icons/redesign/phone-hand-hold-1.svg";
    import webAuthnIcon from "../icons/redesign/video-game-key.svg";
    import passwordIcon from "../icons/redesign/password-type.svg";
    import magicLinkIcon from "../icons/redesign/video-game-magic-wand.svg";
    import LoginOption from "../components/LoginOption.svelte";
    import {links} from "../stores/conf";

    export let id;

    const possibleOptions = [
        {
            key: "useApp",
            icon: appIcon,
            preferred: true
        },
        {
            key: "useWebAuthn",
            icon: webAuthnIcon
        },
        {
            key: "useLink",
            icon: passwordIcon
        },
        {
            key: "usePassword",
            icon: magicLinkIcon
        }
    ]

    let options = [];
    let showSpinner = true;

    onMount(() => {
        $links.displayBackArrow = true;

        knownAccount($user.email).then(res => {
            //res = ["useApp", "useWebAuthn", "useLink", "usePassword"];
            const allOptions = possibleOptions.filter(option => res.includes(option.key));
            if (!allOptions.some(option => option.preferred)) {
                allOptions[0].preferred = true;
            }
            options = allOptions;
            showSpinner = false;
        });
    });
</script>

<style lang="scss">

    h2.header {
        font-size: 24px;
        margin-bottom: 25px;
    }

</style>
{#if showSpinner}
    <Spinner/>
{:else}
    <h2 class="header">{I18n.t("options.header")}</h2>
    {#each options as option, i}
        <LoginOption icon={option.icon}
                     translationKey={option.key}
                     preferred={option.preferred}
                     index={i + 1}
                     route={`/${option.key.toLowerCase()}/${id}`}/>
    {/each}
{/if}
