<script>
    import {user, flash} from "../stores/user";
    import I18n from "i18n-js";
    import {validPassword} from "../validation/regexp";
    import {forgetMe} from "../api";
    import {navigate} from "svelte-routing";
    import chevron_left from "../icons/chevron-left.svg";
    import Button from "../components/Button.svelte";
    import Modal from "../components/Modal.svelte";

    let showModal = false;

    const doForgetMe = showConfirmation => () => {
        if (showConfirmation) {
            showModal = true
        } else {
            forgetMe().then(() => {
                $user.rememberMe = false;
                navigate("/security");
                flash.setValue(I18n.t("rememberMe.updated"));
            });
        }
    };

    const cancel = () => navigate("/security");


</script>

<style>
    .rememberMe {
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

    .options {
        margin-top: 60px;
    }

</style>
<div class="rememberMe">
    <div class="left"></div>
    <div class="inner">
        <div class="header">
            <a href="/back" on:click|preventDefault|stopPropagation={cancel}>
                {@html chevron_left}
            </a>
            <h2>{I18n.t("rememberMe.forgetMeTitle")}</h2>
        </div>
        <p class="info">{I18n.t("rememberMe.info")}</p>
        <div class="options">
            <Button className="cancel" label={I18n.t("rememberMe.cancel")} onClick={cancel}/>

            <Button label={I18n.t("rememberMe.update")}
                    onClick={doForgetMe(true)}/>
        </div>
    </div>

    {#if showModal}
        <Modal submit={doForgetMe(false)}
               cancel={() => showModal = false}
               question={I18n.t("rememberMe.forgetMeConfirmation")}
                       title={I18n.t("rememberMe.forgetMe")}></Modal>
    {/if}
</div>