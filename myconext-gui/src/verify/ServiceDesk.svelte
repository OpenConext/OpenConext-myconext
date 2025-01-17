<script>
    import I18n from "i18n-js";
    import {flash, user} from "../stores/user";
    import Button from "../components/Button.svelte";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg?raw";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import idCard from "../icons/verify/idCard.svg?raw";
    import {isEmpty} from "../utils/utils.js";
    import {createUserControlCode, deleteUserControlCode} from "../api/index.js";
    import {verificationCodeValidityDays} from "../utils/date";
    import {onMount} from "svelte";

    export let toggleView;
    export let cancelView;
    export let showControlCode = false;

    let step = showControlCode ? 2 : 0;
    let lastName = "";
    let firstName = "";
    let dayOfBirth = "";
    let code = "";

    onMount(() => {
        if (showControlCode) {
            lastName = $user.controlCode.lastName;
            firstName = $user.controlCode.lastName;
            dayOfBirth = $user.controlCode.dayOfBirth;
            code = $user.controlCode.code;
        }
    })


    const cancel = () => {
        toggleView();
        step = 0;
        lastName = "";
        firstName = "";
        dayOfBirth = "";
    }

    const backToPersonal = () => {
        cancelView();
    }

    const deleteControlCode = () => {
        deleteUserControlCode()
            .then(() => {
                $user.controlCode = null;
                flash.setValue(I18n.t("verify.serviceDesk.controlCode.deletedControlCode"));
                cancelView();
            })
    }

    const generateControlCode = () => {
        createUserControlCode(firstName, lastName, dayOfBirth)
            .then(res => {
                code = res.code;
                $user.controlCode = res;
                step = 2;
            });
    }

    const init = el => el.focus();

</script>

<style lang="scss">
    h3 {
        color: var(--color-primary-green);
        margin-bottom: 10px;
    }

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

        &.steps, &.important {
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

    .control-code {
        display: flex;
        flex-direction: column;
        background-color: var(--color-secondary-yellow);
        margin: 20px 0;
        padding: 25px;
        border-radius: 4px;
        border: 1px solid var(--color-secondary-grey);

        span {
            margin: auto;
            font-size: 34px;
            letter-spacing: 6px;
        }

        input:disabled {
            background-color: var(--color-primary-yellow);
            border: none;
        }

        .rethink {
            display: flex;
            gap: 2px;
        }
    }

    .button-container {
        margin-bottom: 20px;
        padding-bottom: 30px;
        border-bottom: 1px solid var(--color-primary-grey);
    }

</style>

<div class="info-container">
    {#if step === 0}
        <div class="header-container">
            <span class="back" on:click={cancel} aria-label="toggle-view">
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
    {:else if step === 1}
        <div class="id-card-container">
            <span>{@html idCard}</span>
        </div>
        <p>{I18n.t("verify.serviceDesk.idCard.information")}</p>
        <label for="lastName">{I18n.t("verify.serviceDesk.idCard.lastName")}</label>
        <input id="lastName"
               type="text"
               use:init
               bind:value={lastName}/>
        <label for="firstName">{I18n.t("verify.serviceDesk.idCard.firstName")}</label>
        <input id="firstName"
               type="text"
               bind:value={firstName}/>
        <label for="dayOfBirth">{I18n.t("verify.serviceDesk.idCard.dayOfBirth")}</label>
        <input id="dayOfBirth"
               type="text"
               bind:value={dayOfBirth}/>
        <Button label={I18n.t("verify.serviceDesk.idCard.generateControlCode")}
                fullSize={true}
                disabled={isEmpty(lastName) || isEmpty(firstName) || isEmpty(dayOfBirth)}
                onClick={() => generateControlCode()}/>
    {:else if step === 2}
        <div>
            <h3 class="header">{I18n.t("verify.serviceDesk.controlCode.yourControlCode")}</h3>
            <div class="control-code">
                <span>{code}</span>
            </div>
            <p>{I18n.t("verify.serviceDesk.controlCode.info", {nbr: verificationCodeValidityDays($user.controlCode)})}</p>
            <div class="control-code">
                <label for="lastName">{I18n.t("verify.serviceDesk.idCard.lastName")}</label>
                <input id="lastName"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={lastName}/>
                <label for="firstName">{I18n.t("verify.serviceDesk.idCard.firstName")}</label>
                <input id="firstName"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={firstName}/>
                <label for="dayOfBirth">{I18n.t("verify.serviceDesk.idCard.dayOfBirth")}</label>
                <input id="dayOfBirth"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={dayOfBirth}/>
                <div class="rethink">
                    <p>{I18n.t("verify.serviceDesk.controlCode.typoPrefix")}</p>
                    <a href="/#" on:click|preventDefault|stopPropagation={() => step = 1}>
                        {I18n.t("verify.serviceDesk.controlCode.typoLink")}
                    </a>

                </div>
            </div>
            <p class="important">{I18n.t("verify.serviceDesk.controlCode.todo")}</p>
            <p>{I18n.t("verify.serviceDesk.controlCode.todoDetails")}</p>
            <div class="button-container">
                <Button label={I18n.t("verify.serviceDesk.controlCode.serviceDesks")}
                        fullSize={true}
                        onClick={() => window.open(I18n.t("verify.serviceDesk.controlCode.serviceDesksLocations"), "_blank").focus()}/>
                <Button label={I18n.t("verify.serviceDesk.controlCode.back")}
                        fullSize={true}
                        className="cancel"
                        onClick={backToPersonal}/>
            </div>
            <p>{I18n.t("verify.serviceDesk.controlCode.rethink")}</p>
            <Button label={I18n.t("verify.serviceDesk.controlCode.deleteControlCode")}
                    fullSize={true}
                    deleteAction={true}
                    onClick={deleteControlCode}/>
        </div>
    {/if}
</div>

