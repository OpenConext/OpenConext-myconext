<script>
    import I18n from "i18n-js";
    import studentIcon from "../icons/verify/student.svg";
    import bankIcon from "../icons/verify/bank.svg";
    import Button from "../components/Button.svelte";
    import idinIcon from "../icons/verify/idin-logo.svg";
    import eIDASIcon from "../icons/verify/eIDAS.svg";
    import europeanSvg from "../icons/verify/european.svg";
    import {config} from "../stores/user";
    import closeIcon from "../icons/close_smll.svg";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg";
    import {logo} from "./banks";
    import alertSvg from "../icons/alert-circle.svg";

    export let addInstitution;
    export let addBank;
    export let addEuropean;
    export let cancel;
    export let issuers;

    let showOtherOptions = false;
    let showBankOptions = false;

</script>

<style lang="scss">
    div.account-link-mod {
        padding: 18px 32px;
    }
    div.info-container {
        display: flex;
        flex-direction: column;
        position: relative;

        p {
            margin: 25px 0;
        }

        span.cancel {
            position: absolute;
            right: -6px;
            top: -6px;
            cursor: pointer;

            :global(svg) {
                fill: var(--color-secondary-grey);
                width: 30px;
                height: auto;
            }
        }
    }

    div.header-container {
        display: flex;

        span {
            margin-right: 25px;
        }

        .back {
            cursor: pointer;
        }
    }

    h3 {
        &.header {
            color: var(--color-primary-green);
            margin-bottom: 10px;
        }

    }

    .choice-container {
        border: 1px solid var(--color-secondary-grey);
        border-radius: 4px;
        padding: 15px;
        margin-bottom: 25px;

        h4 {
            font-size: 20px;
            line-height: 24px;
        }

        .choice {
            display: flex;
            gap: 20px;

            :global(svg) {
                margin-left: auto;
            }

        }

        .button-container {
            display: flex;
        }

        :global(a.button) {
            margin: 25px auto 0 0;
        }
    }


    .other-options {
        display: flex;
        padding: 15px;
        cursor: pointer;

        &:hover {
            background-color: var(--color-background);
        }
        p {
            margin: auto;
        }
    }

    .bank-choice {
        display: flex;
        align-items: center;
        margin-bottom: 20px;
        padding: 0;
        border: 2px solid var(--color-primary-blue);
        border-radius: 6px;
        cursor: pointer;

        &:hover {
            background-color: var(--color-background);
        }

        :global(svg) {
            margin-right: 16px;
            width: 48px;
            height: auto;
        }

        span {
            color: var(--color-primary-blue);
            font-weight: bold;
        }
    }

    .alert {
        display: flex;
        align-content: center;
        background-color: #fdf8d3;
        padding: 15px;

        :global(svg.alert-circle) {
            width: 22px;
            height: auto;
            margin-right: 20px;
        }
    }
</style>

<div class="account-link-mod">
    {#if !showBankOptions}
        <div class="info-container">
            <h3 class="header">{I18n.t("verify.modal.info.verify")}</h3>
            <h4 class="info">{I18n.t("verify.modal.info.quick")}</h4>
            <p>{I18n.t("verify.modal.info.please")}</p>
            <span class="cancel" on:click={() => cancel()}>
                {@html closeIcon}
            </span>
        </div>
        <div class="choice-container">
            <div class="choice">
                <h4>{I18n.t("verify.modal.info.educationalInstitution")}</h4>
                {@html studentIcon}
            </div>
            <div class="button-container">
                <Button label={I18n.t("verify.modal.info.selectInstitution")}
                        large={true}
                        onClick={addInstitution}/>
            </div>
        </div>
        {#if !showOtherOptions && $config.idVerify}
            <div class="choice-container other-options" on:click={() => showOtherOptions = !showOtherOptions}>
                <p>{I18n.t("verify.modal.info.other")}</p>
            </div>
        {/if}
        {#if showOtherOptions}
            <div class="choice-container">
                <div class="choice">
                    <h4>{I18n.t("verify.modal.info.verifyBank")}</h4>
                    {@html bankIcon}
                </div>
                <div class="button-container">
                    <Button label={I18n.t("verify.modal.info.selectBank")}
                            icon={idinIcon}
                            custom={true}
                            large={true}
                            onClick={() => showBankOptions = !showBankOptions}/>
                </div>
            </div>
            <div class="choice-container">
                <div class="choice">
                    <h4>{I18n.t("verify.modal.info.verifyEuropeanId")}</h4>
                    {@html europeanSvg}
                </div>
                <p class="support">
                    {I18n.t("verify.modal.info.supportEuropean")}
                </p>
                <div class="button-container">
                    <Button label={I18n.t("verify.modal.info.useEuropean")}
                            icon={eIDASIcon}
                            custom={true}
                            large={true}
                            onClick={addEuropean}/>
                </div>
            </div>
            <div class="disclaimer">
                {@html I18n.t("verify.modal.info.help")}
            </div>
        {/if}
    {/if}
    {#if showBankOptions}
        <div class="info-container">
            <div class="header-container">
            <span class="back" on:click={() => showBankOptions = !showBankOptions}>
                {@html arrowLeftIcon}
            </span>
                <h3 class="header">{I18n.t("verify.modal.bank.select")}</h3>
            </div>
            <p>{@html I18n.t("verify.modal.bank.disclaimer")}</p>
            <span class="cancel" on:click={() => cancel()}>
                {@html closeIcon}
            </span>
        </div>
        <div class="bank-choices-container">
            {#each issuers as issuer}
                <div class="bank-choice" on:click={() => addBank(issuer.id)}>
                    {@html logo(issuer.id)}
                    <span>{issuer.name}</span>
                </div>
            {/each}
        </div>
    {/if}
</div>
{#if showBankOptions}
    <div class="alert">
        {@html alertSvg}
        <span>{I18n.t("verify.modal.bank.anotherMethodPrefix")}
            <a href="/#" on:click|preventDefault|stopPropagation={() => showBankOptions = !showBankOptions}>
                {I18n.t("verify.modal.bank.anotherMethodPostfix")}
            </a>
        </span>

    </div>
{/if}
