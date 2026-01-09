<script>
    import {user} from "../stores/user";

    import {generateCodeExistingUser} from "../api/index";
    import Spinner from "../components/Spinner.svelte";
    import {navigate} from "svelte-routing";
    import {onMount} from "svelte";
    import {isEmpty} from "../utils/utils.js";

    export let id;

    onMount(() => {
        if (isEmpty($user.email)) {
            console.log('Just before redirecting to login', { user: $user})
            debugger;
            navigate(`/login/${id}`);
        } else {
            generateCodeExistingUser($user.email, id)
                .then(() => {
                    navigate(`/code/${id}`, {replace: true});
                }).catch(() => navigate("/expired", {replace: true}));
        }
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
