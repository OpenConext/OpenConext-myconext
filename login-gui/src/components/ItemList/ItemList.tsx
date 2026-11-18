import type { ComponentType } from 'react';

import { handleArrowKeyNavigation } from '@myconext/shared-gui';
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
                        data-testid={`item-list-${key}`}
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
