<script>
    import I18n from "i18n-js";
    import studentIcon from "../icons/verify/student.svg";
    import bankIcon from "../icons/verify/bank.svg";
    import Button from "../components/Button.svelte";
    import idinIcon from "../icons/verify/idin-logo.svg";
    import eIDASIcon from "../icons/verify/eIDAS.svg";
    import europeanSvg from "../icons/verify/european.svg";
    import {config} from "../stores/user";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg";
    import {logo} from "./banks";
    import alertSvg from "../icons/alert-circle.svg";
    import Spinner from "../components/Spinner.svelte";

    export let addInstitution;
    export let addBank;
    export let addEuropean;
    export let issuers;
    export let showIdinOptions;

    let showOtherOptions = false;
    let showBankOptions = false;
    let busyProcessing = false;

    const proceed = action => {
        busyProcessing = true;
        action();
    }

</script>

<style lang="scss">
    div.info-container {
        display: flex;
        flex-direction: column;
        position: relative;

        p {
            margin: 10px 0 20px 0;
            font-size: 16px;
        }

    }

    div.header-container {
        display: flex;

        .back {
            margin-right: 25px;
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

        p.question {
            font-size: 18px;
            font-weight: 600;
        }

        .choice {
            display: flex;
            gap: 20px;

            :global(svg) {
                margin-left: auto;
                color: var(--color-primary-blue);
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

{#if busyProcessing}
    <Spinner/>
{/if}
<div class="account-link-mod">


    {#if !showBankOptions || busyProcessing}
        <div class="info-container">
            <h3 class="header">{I18n.t("verify.modal.info.subheader")}</h3>
            <p>{showIdinOptions ? I18n.t("verify.modal.info.please") : I18n.t("profile.addInstitutionInfo")}</p>
        </div>
        <div class="choice-container">
            <div class="choice">
                <p class="question">{showIdinOptions ? I18n.t("verify.modal.info.educationalInstitution") : I18n.t("profile.addInstitutionAction")}</p>
                {@html studentIcon}
            </div>
            <div class="button-container">
                <Button label={I18n.t("verify.modal.info.selectInstitution")}
                        larger={true}
                        custom={true}
                        disabled={busyProcessing}
                        onClick={() => proceed(addInstitution)}/>
            </div>
        </div>
        {#if !showOtherOptions && $config.featureIdVerify && showIdinOptions}
            <div class="choice-container other-options" on:click={() => showOtherOptions = !showOtherOptions}>
                <p>{I18n.t("verify.modal.info.other")}</p>
            </div>
        {/if}
        {#if showOtherOptions}
            <div class="choice-container">
                <div class="choice">
                    <p class="question">{I18n.t("verify.modal.info.verifyBank")}</p>
                        {@html bankIcon}
                </div>
                <div class="button-container">
                    <Button label={I18n.t("verify.modal.info.selectBank")}
                            icon={idinIcon}
                            custom={true}
                            disabled={busyProcessing}
                            larger={true}
                            onClick={() => showBankOptions = !showBankOptions}/>

                </div>
            </div>
            <div class="choice-container">
                <div class="choice">
                    <p class="question">{I18n.t("verify.modal.info.verifyEuropeanId")}</p>
                    {@html europeanSvg}
                </div>
                <p class="support">
                    {I18n.t("verify.modal.info.supportEuropean")}
                </p>
                <div class="button-container">
                    <Button label={I18n.t("verify.modal.info.useEuropean")}
                            icon={eIDASIcon}
                            custom={true}
                            disabled={busyProcessing}
                            larger={true}
                            onClick={() => proceed(addEuropean)}/>
                </div>
            </div>
            <div class="disclaimer">
                {@html I18n.t("verify.modal.info.help")}
            </div>
        {/if}
    {/if}
    {#if showBankOptions && !busyProcessing}
        <div class="info-container">
            <div class="header-container">
            <span class="back" on:click={() => showBankOptions = !showBankOptions}>
                {@html arrowLeftIcon}
            </span>
                <h3 class="header">{I18n.t("verify.modal.bank.select")}</h3>
            </div>
            <p>{@html I18n.t("verify.modal.bank.disclaimer")}</p>
        </div>
        <div class="bank-choices-container">
            {#each issuers as issuer}
                <div class="bank-choice" on:click={() => proceed(() => addBank(issuer.id))}>
                    {@html logo(issuer)}
                    <span>{issuer.name}</span>
                </div>
            {/each}
        </div>
    {/if}
</div>
{#if showBankOptions && !busyProcessing}
    <div class="alert">
        {@html alertSvg}
        <span>{I18n.t("verify.modal.bank.anotherMethodPrefix")}
            <a href="/#" on:click|preventDefault|stopPropagation={() => showBankOptions = !showBankOptions}>
                {I18n.t("verify.modal.bank.anotherMethodPostfix")}
            </a>
        </span>
    </div>
{/if}
