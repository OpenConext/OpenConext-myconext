<script>
    export let message;
    export let alertType = "sds--alert--status--default";

    $: className = `sds--alert ${alertType.toLowerCase()}`;
</script>

<div class={className}>
    <p>{@html message}</p>
</div>

<style lang="scss">
  :root {
    --sds--color--black: #050E1D;
    --sds--color--white: #FFFFFF;
    --sds--color--gray--500: #5E6873;
    --sds--color--yellow--200: #FEF8D3;
    --sds--color--green--200: #B8E3C9;
    --sds--color--blue--200: #B3E5FF;
    --sds--color--red--200: #FFCDCA;
  }

  .sds--alert {
    padding: 12px;
    display: flex;
    align-items: center;
  }

  .sds--alert--status-default {
    background-color: var(--sds--color--gray--500);
    color: var(--sds--color--white)
  }

  .sds--alert--status-success {
    background-color: var(--sds--color--green--200);
    color: var(--sds--color--black)
  }

  .sds--alert--status-warning {
    background-color: var(--sds--color--yellow--200);
    color: var(--sds--color--black)
  }

  .sds--alert--status-error {
    background-color: var(--sds--color--red--200);
    color: var(--sds--color--black)
  }

  .sds--alert--status-info {
    background-color: var(--sds--color--blue--200);
    color: var(--sds--color--black)
  }
</style>