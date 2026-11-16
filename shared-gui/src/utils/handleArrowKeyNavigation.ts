import type { KeyboardEvent } from 'react';

const KEY_STEP: Partial<Record<string, 1 | -1>> = {
    ArrowDown: 1,
    ArrowRight: 1,
    ArrowUp: -1,
    ArrowLeft: -1,
};

export const handleArrowKeyNavigation = (event: KeyboardEvent<HTMLElement>) => {
    // Combining with a modifier, like Alt + Left or Command + Left, should still work
    if (event.metaKey || event.ctrlKey || event.altKey || event.shiftKey) {
        return;
    }

    const step = KEY_STEP[event.key];
    if (step === undefined) {
        return;
    }

    const buttons = Array.from(
        event.currentTarget.querySelectorAll<HTMLButtonElement>('button'),
    );
    const current = buttons.indexOf(
        document.activeElement as HTMLButtonElement,
    );
    if (current === -1) {
        return;
    }

    event.preventDefault();
    // Wrap around at both ends.
    buttons[(current + step + buttons.length) % buttons.length]?.focus();
};
