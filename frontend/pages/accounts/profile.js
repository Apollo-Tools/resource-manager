import { siteTitle } from '../../components/sidebar';
import Head from 'next/head';

const Profile = () => {
    return (
        <>
            <Head>
                <title>{`${siteTitle}: Profile`}</title>
            </Head>
            Hello Profile
        </>
    );
}

export default Profile;