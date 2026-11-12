import { PageHeader } from '../components/PageHeader';
import I18n from '../locale/I18n';

// Pages of the new design that are not built yet
const Placeholder = ({ titleKey }: { titleKey: string }) => (
    <main className="page-main">
        <PageHeader>{I18n.t(titleKey)}</PageHeader>
    </main>
);

export default Placeholder;
