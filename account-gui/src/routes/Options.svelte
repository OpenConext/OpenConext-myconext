<script>
    import {user} from "../stores/user";
    import I18n from "../locale/I18n";
    import {knownAccount} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {onMount} from "svelte";
    import appIcon from "../icons/redesign/phone-hand-hold-1.svg?raw";
    import webAuthnIcon from "../icons/redesign/video-game-key.svg?raw";
    import passwordIcon from "../icons/redesign/password-type.svg?raw";
    import magicLinkIcon from "../icons/redesign/video-game-magic-wand.svg?raw";
    import LoginOption from "../components/LoginOption.svelte";
    import {links} from "../stores/conf";
    import {navigate} from "svelte-routing";
    import {mrcc} from "../utils/constants";


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
        if (!$user.email) {
            navigate(`/login/${id}`);
        } else {
            knownAccount($user.email)
                .then(res => {
                    //res = ["useApp", "useWebAuthn", "useLink", "usePassword"];
                    const allOptions = possibleOptions.filter(option => res.includes(option.key));
                    if (!allOptions.some(option => option.preferred)) {
                        allOptions[0].preferred = true;
                    }
                    options = allOptions;
                    showSpinner = false;
                }).catch(() => {
                $user.knownUser = null;
                $user.email = null;
                navigate(`/login/${id}`);
            });

        }
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
                     route={`/${option.key.toLowerCase()}/${id}?${mrcc}=true`}/>
    {/each}
{/if}
