import Head from 'next/head';
import { siteTitle } from '../../components/sidebar';

const Resources = () => {
    return (
        <>
            <Head>
                <title>{`${siteTitle}: Resources`}</title>
            </Head>
            Hello Resources
        </>
    );
}

export default Resources;