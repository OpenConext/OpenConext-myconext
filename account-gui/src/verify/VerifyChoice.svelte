<script>
    import I18n from "../locale/I18n";
    import studentIcon from "../icons/verify/student.svg?raw";
    import bankIcon from "../icons/verify/bank.svg?raw";
    import Button from "../components/Button.svelte";
    import idinIcon from "../icons/verify/idin-logo.svg?raw";
    import eIDASIcon from "../icons/verify/eIDAS.svg?raw";
    import europeanSvg from "../icons/verify/european.svg?raw";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg?raw";
    import {logo} from "./banks";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import Spinner from "../components/Spinner.svelte";
    import ServiceDesk from "./ServiceDesk.svelte";

    export let id;
    export let addInstitution;
    export let addBank;
    export let addEuropean;
    export let issuers = [];
    export let showInstitutionOption = true;
    export let otherOptionsAllowed = false;
    export let serviceDeskActive;
    export let serviceName;

    let showOtherOptions = false;
    let showBankOptions = false;
    let busyProcessing = false;
    let showServiceDesk = false;

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
        margin-bottom: 20px;
    }

    div.header-container {
        display: flex;
        align-items: center;
        margin-bottom: 20px;

        span.back {
            margin-right: 25px;
            cursor: pointer;

            :global(svg) {
                width: 24px;
                height: auto;
            }
        }

        h2.header {
            color: var(--color-primary-green);
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
            align-items: center;

            :global(svg) {
                margin-left: auto;
                color: var(--color-primary-blue);
            }

            :global(svg.student-icon) {
                width: 52px;
                height: auto;
            }


        }

        p.support {
            margin-top: 12px;
            font-size: 15px;
            color: #4e4e4e;
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
        text-align: center;

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
            font-weight: 600;
        }
    }

    .alert {
        display: flex;
        align-content: center;
        background-color: #fdf8d3;
        padding: 15px;

        :global(svg.alert-circle) {
            width: 48px;
            height: auto;
            margin-right: 16px;
        }
    }
</style>

{#if busyProcessing}
    <Spinner/>
{/if}
<div class="account-link-mod">
    {#if !showServiceDesk && (!showBankOptions || busyProcessing)}
        <div class="info-container">
            <h2 class="header">{I18n.t("verify.modal.info.verify")}</h2>
        </div>
        {#if showInstitutionOption}
            <div class="choice-container">
                <div class="choice">
                    <h4>{I18n.t("VerifyIdentity.VerifyViaDutchInstitution.Title.COPY")}</h4>
                    {@html studentIcon}
                </div>
                <div class="button-container">
                    <Button label={I18n.t("VerifyIdentity.VerifyViaDutchInstitution.Button.COPY")}
                            large={true}
                            disabled={busyProcessing}
                            onClick={() => proceed(addInstitution)}/>
                </div>
            </div>
        {/if}
        {#if otherOptionsAllowed && !showOtherOptions && showInstitutionOption}
            <div class="choice-container other-options" on:click={() => showOtherOptions = !showOtherOptions}>
                <p>{I18n.t("verify.modal.info.other")}</p>
            </div>
        {/if}
        {#if otherOptionsAllowed && (showOtherOptions || !showInstitutionOption)}
            <div class="choice-container">
                <div class="choice">
                    <h4>{I18n.t("VerifyIdentity.VerifyWithBankApp.Title.COPY")}</h4>
                    {@html bankIcon}
                </div>
                <div class="button-container">
                    <Button label={I18n.t("VerifyIdentity.VerifyWithBankApp.Button.COPY")}
                            icon={idinIcon}
                            custom={true}
                            disabled={busyProcessing}
                            large={true}
                            onClick={() => showBankOptions = !showBankOptions}/>

                </div>
            </div>
            <div class="choice-container">
                <div class="choice">
                    <h4>{I18n.t("VerifyIdentity.VerifyWithAEuropianId.Title.COPY")}</h4>
                    {@html europeanSvg}
                </div>
                <p class="support">
                    {I18n.t("verify.modal.info.supportEuropean")}
                </p>
                <div class="button-container">
                    <Button label={I18n.t("VerifyIdentity.VerifyWithAEuropianId.Button.COPY")}
                            icon={eIDASIcon}
                            custom={true}
                            disabled={busyProcessing}
                            large={true}
                            onClick={() => proceed(addEuropean)}/>
                </div>
            </div>
            {#if serviceDeskActive}
                <div class="choice-container other-options" on:click={() => showServiceDesk = !showServiceDesk}>
                    <p>{I18n.t("verify.modal.info.cantUse")}</p>
                </div>
            {/if}
        {/if}
    {/if}
    {#if showBankOptions && !busyProcessing && !showServiceDesk}
        <div class="info-container">
            <div class="header-container">
            <span class="back" on:click={() => showBankOptions = !showBankOptions}>
                {@html arrowLeftIcon}
            </span>
                <h2 class="header">{I18n.t("VerifyIdentity.VerifyWithBankApp.Button.COPY")}</h2>
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
{#if showBankOptions && !busyProcessing && !showServiceDesk}
    <div class="alert">
        {@html alertSvg}
        <span>{I18n.t("verify.modal.bank.anotherMethodPrefix")}
            <a href="/#" on:click|preventDefault|stopPropagation={() => showBankOptions = !showBankOptions}>
                {I18n.t("SelectYourBank.BankNotInList.HighlightedPart.COPY")}
            </a>
        </span>
    </div>
{/if}
{#if !busyProcessing && showServiceDesk}
    <ServiceDesk id={id}
                 serviceName={serviceName}
                 toggleView={() => showServiceDesk = false}
    />
{/if}

