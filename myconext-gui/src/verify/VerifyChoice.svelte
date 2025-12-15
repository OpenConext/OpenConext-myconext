<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";
    import {config} from "../stores/user";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg?raw";
    import {logo} from "./banks";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import Spinner from "../components/Spinner.svelte";
    import ServiceDesk from "./ServiceDesk.svelte";

    import idinSvg from "../icons/verify/idin.svg?raw";
    import eIDASSvg from "../icons/verify/eIDAS/eIDAS logo, Monochromatisch, wit.svg?raw";
    import customerSupportSvg from "../icons/verify/headphones-customer-support-human 1.svg?raw";

    export let addInstitution;
    export let addBank;
    export let addEuropean;
    export let issuers;
    export let showIdinOptions;
    export let cancel;
    export let showServiceDesk = false;
    export let showControlCode = false;

    let showBankOptions = false;
    let busyProcessing = false;

    const proceed = action => {
        busyProcessing = true;
        action();
    }

    $: shouldShowServiceDesk = showBankOptions && !busyProcessing && !showServiceDesk && !showControlCode;
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

    div.info-id-verify-container {
      display: flex;
      flex-direction: column;
      position: relative;

      p {
        margin: 25px auto 0 0;
        font-size: 16px;
      }
    }

    .alert-info {
      display: flex;
      flex-direction: column;
      position: relative;
      background-color: #DFF4FF;
      padding: 15px;

      div.content {
        display: flex;
        flex-direction: column;

        p.eea-note {
          font-size: 12px;
        }
      }

      span {
        line-height: 22px;
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
            font-weight: 600;
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
    :global(a.button) {
      margin: 25px auto 0 0;
    }
</style>

{#if busyProcessing}
    <Spinner/>
{/if}
<div class="account-link-mod">
    {#if !showServiceDesk && !showControlCode && (!showBankOptions || busyProcessing)}
        <div class="info-container">
            <p>{showIdinOptions ? I18n.t("verify.modal.info.please") : I18n.t("VerifyIdentity.SubtitleHasInternalLink.COPY")}</p>
        </div>
        <div class="alert-info">
            <p class="question">{showIdinOptions
                ? I18n.t("VerifyIdentity.VerifyViaDutchInstitution.Title.COPY")
                : I18n.t("VerifyIdentity.VerifyViaDutchInstitution.TitleHasInternalLink.COPY")}</p>
            <div class="button-container">
                <Button label={I18n.t("VerifyIdentity.VerifyViaDutchInstitution.Button.COPY")}
                        larger={true}
                        custom={false}
                        disabled={busyProcessing}
                        onClick={() => proceed(addInstitution)}
                        fullSize="true"
                />
            </div>
        </div>
        {#if $config.featureIdVerify && showIdinOptions && !showServiceDesk && !showControlCode}
            <div class="info-id-verify-container">
                <p>{I18n.t("VerifyIdentity.VerifyViaOptions.Title.COPY")}</p>
            </div>
            <div class="button-container">
                <Button label={I18n.t("VerifyIdentity.VerifyWithBankApp.Button.COPY")}
                        icon={idinSvg}
                        custom={false}
                        disabled={busyProcessing}
                        larger={true}
                        onClick={() => showBankOptions = !showBankOptions}
                        className="secondary"
                        fullSize="true"
                        big="true"
                />
            </div>
            <div class="button-container">
                <Button label={I18n.t("VerifyIdentity.VerifyWithAEuropianId.Button.COPY")}
                        icon={eIDASSvg}
                        custom={false}
                        disabled={busyProcessing}
                        larger={true}
                        onClick={() => proceed(addEuropean)}
                        className="secondary"
                        fullSize="true"
                        big="true"
                />
            </div>
            {#if $config.featureServiceDeskActive}
                <div class="button-container">
                    <Button label={I18n.t("verify.modal.info.cantUse")}
                            icon={customerSupportSvg}
                            custom={false}
                            disabled={busyProcessing}
                            larger={true}
                            onClick={() => showServiceDesk = !showServiceDesk}
                            className="secondary"
                            fullSize="true"
                            big="true"
                    />
                </div>
            {/if}

        {/if}
    {/if}
    {#if shouldShowServiceDesk}
        <div class="info-container">
            <div class="header-container">
            <span class="back" on:click={() => showBankOptions = !showBankOptions}>
                {@html arrowLeftIcon}
            </span>
                <h3 class="header">{I18n.t("VerifyIdentity.VerifyWithBankApp.Button.COPY")}</h3>
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
{#if shouldShowServiceDesk}
    <div class="alert">
        {@html alertSvg}
        <span>{I18n.t("verify.modal.bank.anotherMethodPrefix")}
            <a href="/#" on:click|preventDefault|stopPropagation={() => showBankOptions = !showBankOptions}>
                {I18n.t("SelectYourBank.BankNotInList.HighlightedPart.COPY")}
            </a>
        </span>
    </div>
{/if}
{#if !busyProcessing && (showServiceDesk || showControlCode)}
    <ServiceDesk toggleView={() => showServiceDesk = false}
                 cancelView={cancel}
                 showControlCode={showControlCode}/>
{/if}

