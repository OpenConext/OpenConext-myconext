import "core-js/stable";
import "regenerator-runtime/runtime";
import { mount } from 'svelte';
import App from "./App.svelte";

import "./locale/en";
import "./locale/nl";

// const app = new App({target: document.body});
const app = mount(App, { target: document.body });
export default app;
