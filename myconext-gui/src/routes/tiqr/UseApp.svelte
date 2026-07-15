<script>
    import {navigate} from "svelte-routing";
    import TiqrAuthentication from "../../components/TiqrAuthentication.svelte";
    import {authenticationStatus} from "../../constants/authenticationStatus";

    const onDone = ({status}) => {
        console.log("TiqrAuth is done, final status = ",status );
        if (status === authenticationStatus.SUCCESS) {
            navigate("/change-recovery");
        }
    }
</script>

<style lang="scss">

    .use-app {
        width: 100%;
        display: flex;
        flex-direction: column;
        height: 100%;
        padding: 15px 30px 15px 0;
    }

</style>

<div class="use-app">
    <TiqrAuthentication onDone={onDone}/>
</div>
