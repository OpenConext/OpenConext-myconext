import "core-js/stable";
import "regenerator-runtime/runtime";
import { mount } from 'svelte';
import App from "./App.svelte";

import "./locale/en";
import "./locale/nl";

const app = mount(App, { target: document.body });
export default app;