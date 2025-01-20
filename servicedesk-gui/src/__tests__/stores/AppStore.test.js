import { assert, expect, test } from 'vitest'
import {useAppStore} from "../../stores/AppStore";

test("Store outside functional component", () => {
    const csrfToken = useAppStore.getState().csrfToken;
    expect(csrfToken).toBeUndefined();

    useAppStore.setState({csrfToken: "test"});

    const updatedCsrfToken = useAppStore.getState().csrfToken;
    expect(updatedCsrfToken).toEqual("test");
});