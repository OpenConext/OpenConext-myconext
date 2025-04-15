<script>
    import {user} from "../stores/user";

    import {generateCodeExistingUser} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";

    export let id;

    onMount(() => {
        generateCodeExistingUser($user.email, id)
            .then(() => {
                navigate(`/code/${id}`, {replace: true});
            }).catch(() => navigate("/expired", {replace: true}));
    });

</script>
<style lang="scss">
    div.spinner-container {
        display: flex;
        flex-direction: column;

        p {
            margin: 75px auto 0 auto;
        }
    }
</style>
<div class="spinner-container">
    <Spinner/>
</div>
