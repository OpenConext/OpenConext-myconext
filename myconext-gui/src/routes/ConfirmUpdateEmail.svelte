<script>
    import {flash, user} from "../stores/user";
    import {onMount} from "svelte";
    import {confirmEmail} from "../api";
    import {navigate} from "svelte-routing";
    import Spinner from "../components/Spinner.svelte";
    import I18n from "../locale/I18n";

    onMount(() => {
        const urlSearchParams = new URLSearchParams(window.location.search);
        const hash = urlSearchParams.get("h");
        const nav = urlSearchParams.get("nav");
        confirmEmail(hash)
            .then(json => {
                for (var key in json) {
                    if (json.hasOwnProperty(key)) {
                        $user[key] = json[key];
                    }
                }
                flash.setValue(I18n.t("Email.Confirmed.COPY", {email: $user.email}));
                navigate(`/${nav ? "security" : "personal"}`);
            })
            .catch(() => navigate("/404"))
    });

</script>

<Spinner/>
