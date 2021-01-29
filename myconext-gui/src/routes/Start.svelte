<script>
    import I18n from "i18n-js";
    import {navigate} from "svelte-routing";
    import {user} from "../stores/user";
    import personalInfoSvg from "../icons/redesign/Personal_info.svg";
    import dataActivitySvg from "../icons/redesign/data_activity.svg";
    import securitySvg from "../icons/redesign/Security.svg";
    import settingsSvg from "../icons/redesign/settings.svg";

</script>

<style lang="scss">
  .start {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    margin-bottom: 80px;
  }

  h2 {
    margin: 20px 0 10px 0;
    color: var(--color-primary-green);
  }

  p {
    font-weight: 300;
    margin-bottom: 26px;
  }

  .card-container {
    margin: 0 auto;
    display: grid;
    width: 100%;
    grid-template-columns: repeat(2, 50%);
    grid-template-rows: repeat(2, 50%);
    grid-gap: 25px;

    @media (max-width: 820px) {
      width: 95%;
      grid-template-columns: 100%;
      grid-template-rows: repeat(4, 25%);
    }
  }

  .card {
    padding: 40px 0 25px 0;
    box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.5);
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;

    :global(svg) {
      width: 160px;
      height: auto;
    }

    h3 {
      margin-top: auto;
      font-size: 22px;
    }
  }


</style>
<div class="start">
    <h2>{I18n.t("start.hi", {name: $user.givenName})}</h2>
    <p>{I18n.t("start.manage")}</p>
    <div class="card-container">
        <div class="card" on:click={() => navigate("/personal")}>
            {@html personalInfoSvg}
            <h3>{I18n.t("sidebar.personalInfo")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/data-activity")}>
            {@html dataActivitySvg}
            <h3>{I18n.t("sidebar.dataActivity")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/security")}>
            {@html securitySvg}
            <h3>{I18n.t("sidebar.security")}</h3>
        </div>
        <div class="card" on:click={() => navigate("/account")}>
            {@html settingsSvg}
            <h3>{I18n.t("sidebar.account")}</h3>
        </div>

    </div>

</div>