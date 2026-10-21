import { GuestLayout } from '../../components/GuestLayout';
import { LinkErrorPage } from '../../components/LinkErrorPage';

export default function CreateFromInstitutionEppnAlreadyLinked() {
    return (
        <GuestLayout>
            <LinkErrorPage variant="eppnAlreadyLinked" />
        </GuestLayout>
    );
}
