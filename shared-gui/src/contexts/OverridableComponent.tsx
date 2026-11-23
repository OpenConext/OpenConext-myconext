import { isValidElement } from 'react';
import type { FC, ReactNode } from 'react';

import {
    type OverridableComponentProps,
    useCustomization,
} from './customizationContext';

const altFromDefault = (children: ReactNode): string => {
    if (
        isValidElement<{ alt?: unknown }>(children) &&
        children.type === 'img' &&
        typeof children.props.alt === 'string'
    ) {
        return children.props.alt;
    }
    return '';
};

const isIntrinsicTag = (type: string): boolean =>
    /^[a-z][a-z0-9-]*$/.test(type);

// Customization modules written for SVGR do `<Logo />` on an svg import.
// This bundler serves that import as a URL or a data URL. React would use
// the string as a tag name and throw InvalidCharacterError.
const svgImportElement = (node: ReactNode, alt: string): ReactNode => {
    if (!isValidElement(node) || typeof node.type !== 'string') {
        return node;
    }
    if (isIntrinsicTag(node.type)) {
        return node;
    }
    return <img src={node.type} alt={alt} />;
};

export const OverridableComponent: FC<OverridableComponentProps> = ({
    appCustomizationReactNodeKey,
    children,
}) => {
    const comp = useCustomization()[appCustomizationReactNodeKey];
    // `undefined` is the only "no override" value. `null` clears the slot.
    if (comp === undefined || comp instanceof Promise) {
        return children;
    }
    return svgImportElement(comp, altFromDefault(children));
};
