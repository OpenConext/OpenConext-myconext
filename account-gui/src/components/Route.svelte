<!--
  Local replacement for `svelte-routing`'s `Route.svelte`.

  `svelte-routing@2.13.0` ships a Svelte-4-only heuristic that decides whether
  the `component` prop is a component class or a lazy-loading factory:

      if (c.toString().startsWith("class ")) component = c;
      else component = c();

  Svelte 5 compiles components to plain functions (not classes), so the check
  is always false and the library invokes the component as a function without
  an `$$anchor`. That yields garbage which flows into `<svelte:component>` and
  trips the Svelte ≥5.55 HMR-anchor logic with:

      Cannot read properties of undefined (reading 'Symbol(hmr anchor)')

  This file uses the existing Router context from `svelte-routing` and replaces
  the heuristic with one that works for Svelte 5: only call `c()` when it's a
  zero-arity function (the canonical lazy pattern `() => import("./X.svelte")`).
-->
<script>
    import { onDestroy } from "svelte";
    import { useRouter } from "svelte-routing";

    export let path = "";
    export let component = null;

    const canUseDOM = () =>
        typeof window !== "undefined" &&
        "document" in window &&
        "location" in window;

    let routeParams = {};
    let routeProps = {};

    const { registerRoute, unregisterRoute, activeRoute } = useRouter();

    const route = {
        path,
        default: path === "",
    };

    $: if ($activeRoute && $activeRoute.route === route) {
        routeParams = $activeRoute.params;

        const { component: c, path, ...rest } = $$props;
        routeProps = rest;

        if (c) {
            if (typeof c === "function" && c.length === 0) {
                component = c();
            } else {
                component = c;
            }
        }

        canUseDOM() && !$activeRoute.preserveScroll && window?.scrollTo(0, 0);
    }

    registerRoute(route);

    onDestroy(() => {
        unregisterRoute(route);
    });
</script>

{#if $activeRoute && $activeRoute.route === route}
    {#if component}
        {#await component then resolvedComponent}
            <svelte:component
                this={resolvedComponent?.default || resolvedComponent}
                {...routeParams}
                {...routeProps}
            />
        {/await}
    {:else}
        <slot params={routeParams} />
    {/if}
{/if}
