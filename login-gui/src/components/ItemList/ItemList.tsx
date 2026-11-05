import type { ComponentType, KeyboardEvent } from 'react';

import type { IconProps } from '@phosphor-icons/react';
import {
    Item,
    ItemContent,
    ItemGroup,
    ItemMedia,
    ItemTitle,
} from '@surfnet/curve-react';

import styles from './ItemList.module.scss';

export interface ItemListEntry {
    key: string;
    icon: ComponentType<IconProps>;
    label: string;
    onClick: () => void;
}

interface ItemListProps {
    items: ItemListEntry[];
}

const KEY_STEP: Partial<Record<string, 1 | -1>> = {
    ArrowDown: 1,
    ArrowRight: 1,
    ArrowUp: -1,
    ArrowLeft: -1,
};

export const handleArrowKeyNavigation = (event: KeyboardEvent<HTMLElement>) => {
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

export const ItemList = ({ items }: ItemListProps) => (
    <ItemGroup onKeyDown={handleArrowKeyNavigation}>
        {items.map(({ key, icon: Icon, label, onClick }, index) => (
            <Item
                key={key}
                variant="outline"
                className={styles.item}
                render={
                    <button
                        type="button"
                        onClick={onClick}
                        autoFocus={index === 0}
                    />
                }
            >
                <ItemMedia>
                    <Icon aria-hidden />
                </ItemMedia>
                <ItemContent>
                    <ItemTitle>{label}</ItemTitle>
                </ItemContent>
            </Item>
        ))}
    </ItemGroup>
);
