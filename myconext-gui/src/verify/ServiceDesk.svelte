<script>
    import I18n from "i18n-js";
    import Button from "../components/Button.svelte";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg?raw";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import idCard from "../icons/verify/idCard.svg?raw";

    export let toggleView;

    let step = 0;
    let lastName = "";
    let firstName = "";
    let dateOfBirth = "";

    const generateControlCode = () => {

    }

    const proceed = () => {

    }

</script>

<style lang="scss">
    div.info-container {
        display: flex;
        flex-direction: column;
        position: relative;
        margin-bottom: 20px;

        :global(sup) {
            vertical-align: top;
            font-size: 16px;
        }
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

    p {
        margin-bottom: 20px;

        &.steps {
            font-weight: 600;
        }
    }

    ol {
        list-style: decimal;

        li {
            margin: 0 0 20px 20px;
        }
    }

    ul {
        list-style: '- ' inside;
        margin-bottom: 20px;
    }

    .button-container {
        display: flex;
    }

    :global(a.button) {
        margin: 25px auto 0 0;
    }

    .redirect {
        display: flex;
        align-items: flex-start;
        background-color: #fdf8d3;
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

        :global(svg.alert-circle) {
            width: 42px;
            height: auto;
            margin-right: 16px;
        }
    }

    div.id-card-container {
        display: flex;
        margin-bottom: 20px;
        span {
            margin: auto;
        }
    }

    label {
        font-weight: 600;
        margin: 5px 0 5px 0;
        display: inline-block;
    }

    input {
        border-radius: 8px;
        border: solid 1px #676767;
        padding: 14px;
        font-size: 16px;
        margin-bottom: 20px;
    }

</style>

<div class="info-container">
    {#if step === 0}
        <div class="header-container">
            <span class="back" on:click={() => toggleView()}>
                {@html arrowLeftIcon}
            </span>
            <h2 class="header">{I18n.t("verify.serviceDesk.confirmIdentityHeader")}</h2>
        </div>
        <p>{I18n.t("verify.serviceDesk.confirmIdentity")}</p>
        <p class="steps">{I18n.t("verify.serviceDesk.stepsHeader")}</p>
        <ol>
            <li>{I18n.t("verify.serviceDesk.step1")}</li>
            <li>{I18n.t("verify.serviceDesk.step2")}</li>
            <li>{I18n.t("verify.serviceDesk.step3")}</li>
        </ol>
        <div class="redirect">
            {@html alertSvg}
            <div class="content">
                <span>{@html I18n.t("verify.serviceDesk.acceptedIds")}</span>
                <ul>
                    <li>{@html I18n.t("verify.serviceDesk.passports")}</li>
                    <li>{@html I18n.t("verify.serviceDesk.eea")}</li>
                    <li>{@html I18n.t("verify.serviceDesk.driverLicense")}</li>
                    <li>{@html I18n.t("verify.serviceDesk.residencePermit")}</li>
                </ul>
                <p>{I18n.t("verify.serviceDesk.note")}</p>
                <p class="eea-note">{@html I18n.t("verify.serviceDesk.eeaNote")}</p>
            </div>
        </div>
        <Button label={I18n.t("verify.serviceDesk.next")}
                fullSize={true}
                onClick={() => step = 1}/>
    {:else if (step === 1)}
        <div class="id-card-container">
            <span>{@html idCard}</span>
        </div>
        <p>{I18n.t("verify.serviceDesk.idCard.information")}</p>
        <label for="lastName">{I18n.t("verify.serviceDesk.idCard.lastName")}</label>
        <input id="lastName"
               type="text"
               bind:value={lastName}/>
        <label for="firstName">{I18n.t("verify.serviceDesk.idCard.firstName")}</label>
        <input id="firstName"
               type="text"
               bind:value={firstName}/>
        <label for="dateOfBirth">{I18n.t("verify.serviceDesk.idCard.dateOfBirth")}</label>
        <input id="dateOfBirth"
               type="text"
               bind:value={dateOfBirth}/>

        <Button label={I18n.t("verify.serviceDesk.idCard.generateControlCode")}
                fullSize={true}
                onClick={() => generateControlCode()}/>
    {/if}
</div>

