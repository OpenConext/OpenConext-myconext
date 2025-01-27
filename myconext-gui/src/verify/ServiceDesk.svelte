<script>
    import I18n from "../locale/I18n";
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
                flash.setValue(I18n.t("ServiceDesk.ControlCode.DeletedControlCode.COPY"));
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
            <h2 class="header">{I18n.t("ServiceDesk.ConfirmIdentityHeader.COPY")}</h2>
        </div>
        <p>{I18n.t("ServiceDesk.ConfirmIdentity.COPY")}</p>
        <p class="steps">{I18n.t("ServiceDesk.StepsHeader.COPY")}</p>
        <ol>
            <li>{I18n.t("ServiceDesk.Step1.COPY")}</li>
            <li>{I18n.t("ServiceDesk.Step2.COPY")}</li>
            <li>{I18n.t("ServiceDesk.Step3.COPY")}</li>
        </ol>
        <div class="redirect">
            {@html alertSvg}
            <div class="content">
                <span>{@html I18n.t("ServiceDesk.AcceptedIds.COPY")}</span>
                <ul>
                    <li>{@html I18n.t("ServiceDesk.Passports.COPY")}</li>
                    <li>{@html I18n.t("ServiceDesk.Eea.COPY")}</li>
                    <li>{@html I18n.t("ServiceDesk.DriverLicense.COPY")}</li>
                    <li>{@html I18n.t("ServiceDesk.ResidencePermit.COPY")}</li>
                </ul>
                <p>{I18n.t("ServiceDesk.Note.COPY")}</p>
                <p class="eea-note">{@html I18n.t("ServiceDesk.EeaNote.COPY")}</p>
            </div>
        </div>
        <Button label={I18n.t("ServiceDesk.Next.COPY")}
                fullSize={true}
                onClick={() => step = 1}/>
    {:else if step === 1}
        <div class="id-card-container">
            <span>{@html idCard}</span>
        </div>
        <p>{I18n.t("ServiceDesk.IdCard.Information.COPY")}</p>
        <label for="lastName">{I18n.t("ServiceDesk.IdCard.LastName.COPY")}</label>
        <input id="lastName"
               type="text"
               use:init
               bind:value={lastName}/>
        <label for="firstName">{I18n.t("ServiceDesk.IdCard.FirstName.COPY")}</label>
        <input id="firstName"
               type="text"
               bind:value={firstName}/>
        <label for="dayOfBirth">{I18n.t("ServiceDesk.IdCard.DayOfBirth.COPY")}</label>
        <input id="dayOfBirth"
               type="text"
               bind:value={dayOfBirth}/>
        <Button label={I18n.t("ServiceDesk.IdCard.GenerateControlCode.COPY")}
                fullSize={true}
                disabled={isEmpty(lastName) || isEmpty(firstName) || isEmpty(dayOfBirth)}
                onClick={() => generateControlCode()}/>
    {:else if step === 2}
        <div>
            <h3 class="header">{I18n.t("ServiceDesk.ControlCode.YourControlCode.COPY")}</h3>
            <div class="control-code">
                <span>{code}</span>
            </div>
            <p>{I18n.t("ServiceDesk.ControlCode.Info.COPY", {nbr: verificationCodeValidityDays($user.controlCode)})}</p>
            <div class="control-code">
                <label for="lastName">{I18n.t("ServiceDesk.IdCard.LastName.COPY")}</label>
                <input id="lastName"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={lastName}/>
                <label for="firstName">{I18n.t("ServiceDesk.IdCard.FirstName.COPY")}</label>
                <input id="firstName"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={firstName}/>
                <label for="dayOfBirth">{I18n.t("ServiceDesk.IdCard.DayOfBirth.COPY")}</label>
                <input id="dayOfBirth"
                       type="text"
                       class="read-only"
                       disabled="true"
                       bind:value={dayOfBirth}/>
                <div class="rethink">
                    <p>{I18n.t("ServiceDesk.ControlCode.TypoPrefix.COPY")}</p>
                    <a href="/#" on:click|preventDefault|stopPropagation={() => step = 1}>
                        {I18n.t("ServiceDesk.ControlCode.TypoLink.COPY")}
                    </a>

                </div>
            </div>
            <p class="important">{I18n.t("ServiceDesk.ControlCode.Todo.COPY")}</p>
            <p>{I18n.t("ServiceDesk.ControlCode.TodoDetails.COPY")}</p>
            <div class="button-container">
                <Button label={I18n.t("ServiceDesk.ControlCode.ServiceDesks.COPY")}
                        fullSize={true}
                        onClick={() => window.open(I18n.t("ServiceDesk.ControlCode.ServiceDesksLocations.COPY"), "_blank").focus()}/>
                <Button label={I18n.t("ServiceDesk.ControlCode.Back.COPY")}
                        fullSize={true}
                        className="cancel"
                        onClick={backToPersonal}/>
            </div>
            <p>{I18n.t("ServiceDesk.ControlCode.Rethink.COPY")}</p>
            <Button label={I18n.t("ServiceDesk.ControlCode.DeleteControlCode.COPY")}
                    fullSize={true}
                    deleteAction={true}
                    onClick={deleteControlCode}/>
        </div>
    {/if}
</div>

