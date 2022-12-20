import { siteTitle } from '../../components/Sidebar';
import Head from 'next/head';

const Reservations = () => {
    return (
        <>
            <Head>
                <title>{`${siteTitle}: Reservations`}</title>
            </Head>
            Hello Reservations
        </>
    );
}

export default Reservations;