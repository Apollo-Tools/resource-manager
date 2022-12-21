import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { useEffect, useState } from 'react';
import { useAuth } from '../../lib/AuthenticationProvider';
import NewResourceForm from '../../components/NewResourceForm';


const NewResource = () => {
    const {token} = useAuth();
    const [resource, setNewResource] = useState();

    useEffect(() => {
        if (resource !== null) {
            console.log("new resource " + resource);
        }
    }, [resource])

    return (
        <>
            <Head>
                <title>{`${siteTitle}: New Resource`}</title>
            </Head>
            <NewResourceForm setNewResource={setNewResource} token={token}/>
        </>
    );
}

export default NewResource;