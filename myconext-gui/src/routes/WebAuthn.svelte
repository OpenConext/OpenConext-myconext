<script>
    import {onMount} from "svelte";
    import { create, get, supported } from "@github/webauthn-json"
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {me, updateSecurity} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";

    let response = {};

    let usePublicKey = $user.usePublicKey;

    onMount(() => {
        create({
            publicKey: {
                challenge: "CCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCCC",
                rp: { name: "Localhost, Inc." },
                user: { id: "IIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIIII", name: "test_user", displayName: "Test User" },
                pubKeyCredParams: [{ type: "public-key", alg: -7 }],
                excludeCredentials: []
            }
        }).then(res => {
          debugger;
          response = res;
        });
    });

    const update = () => {}

    const cancel = () => {
        me().then(json => {
            for (var key in json) {
                if (json.hasOwnProperty(key)) {
                    $user[key] = json[key];
                }
            }
            navigate("/security");
        });
    }


</script>

<style>
    .password {
        width: 100%;
        display: flex;
        height: 100%;
    }

    @media (max-width: 820px) {
        .left {
            display: none;
        }

        .inner {
            border-left: none;
        }
    }

    .left {
        background-color: #f3f6f8;
        width: 270px;
        height: 100%;
        border-bottom-left-radius: 8px;
    }

    .inner {
        margin: 20px 0 190px 0;
        padding: 15px 15px 0 40px;
        border-left: 2px solid var(--color-primary-grey);
        display: flex;
        flex-direction: column;
        background-color: white;
    }

    .header {
        display: flex;
        align-items: center;
        align-content: center;
        color: var(--color-primary-green);
    }

    .header a {
        margin-top: 8px;
    }

    h2 {
        margin-left: 25px;
    }

    p.info {
        margin: 12px 0 32px 0;
    }

    label {
        font-weight: bold;
        margin: 33px 0 13px 0;
        display: inline-block;
    }

    input {
        border-radius: 8px;
        border: solid 1px #676767;
        padding: 14px;
        font-size: 16px;
    }

    span.error {
        margin-top: 5px;
        display: inline-block;
        color: var(--color-primary-red);
    }

    .options {
        margin-top: 60px;
    }

    :global(.options a:not(:first-child)) {
        margin-left: 25px;
    }


</style>
<div class="password">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{usePublicKey ? I18n.ts("password.updateTitle") : I18n.ts("password.setTitle")}</h2>
        </div>
        <p class="info">{I18n.t("password.passwordDisclaimer")}</p>

        <input id="username" autocomplete="username email" type="hidden" name="username" value={$user.email}>

        <textarea name="" id="" cols="30" rows="10">
            {JSON.stringify(response)}
        </textarea>

        <div class="options">
            <Button className="cancel" label={I18n.ts("password.cancel")} onClick={cancel}/>

            <Button label={usePublicKey ? I18n.ts("password.updateUpdate") : I18n.ts("password.setUpdate")}
                    onClick={update}
                    disabled={false}/>
        </div>
    </div>


</div>