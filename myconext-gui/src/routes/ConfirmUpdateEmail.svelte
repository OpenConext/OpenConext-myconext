<script>
    import {user, flash} from "../stores/user";
    import {onMount} from "svelte";
    import {confirmEmail} from "../api";
    import {navigate} from "svelte-routing";
    import Spinner from "../components/Spinner.svelte";
    import I18n from "i18n-js";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const hash = urlSearchParams.get("h");
        confirmEmail(hash)
            .then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                flash.setValue(I18n.t("email.confirmed", {email: $user.email}));
                navigate("/personal");
            })
            .catch(() => navigate("/404"))
    });

</script>

<Spinner/>
