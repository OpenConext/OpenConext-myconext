<script>
    import I18n from "../locale/I18n";
    import Button from "../components/Button.svelte";
    import arrowLeftIcon from "../icons/verify/arrow-left.svg?raw";
    import alertSvg from "../icons/alert-circle.svg?raw";
    import {conf} from "../stores/conf";

    export let toggleView;
    export let serviceName;
    export let id;

    const proceed = () => {
        window.location.href = `${$conf.basePath}/servicedesk/${id}`;
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

        span {
            line-height: 22px;
        }

        :global(svg.alert-circle) {
            width: 98px;
            height: auto;
            margin-right: 16px;
        }
    }
</style>

<div class="info-container">
    <div class="header-container">
            <span class="back" on:click={() => toggleView()}>
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
        <span>{@html I18n.t("ServiceDesk.RedirectWarning.COPY", {service: serviceName})}</span>
    </div>
    <Button label={I18n.t("ServiceDesk.Next.COPY")}
            large={true}
            onClick={() => proceed()}/>

</div>

