import { useParams } from 'react-router';

export default function Code() {
    const { id } = useParams();
    return <h1>{id}</h1>;
}
