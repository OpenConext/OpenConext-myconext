import { GuestLayout } from '../../components/GuestLayout';
import { LinkErrorPage } from '../../components/LinkErrorPage';

export default function CreateFromInstitutionAttributeMissing() {
    return (
        <GuestLayout>
            <LinkErrorPage variant="attributeMissing" />
        </GuestLayout>
    );
}
