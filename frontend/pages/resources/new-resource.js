import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { useEffect, useState } from 'react';
import { useAuth } from '../../lib/AuthenticationProvider';
import NewResourceForm from '../../components/NewResourceForm';
import AddMetricValuesForm from '../../components/AddMetricValuesForm';


const NewResource = () => {
    const {token} = useAuth();
    const [newResource, setNewResource] = useState(null);

    useEffect(() => {
        if (newResource != null) {
            console.log("new resource " + newResource);
        }
    }, [newResource])

    return (
        <>
            <Head>
                <title>{`${siteTitle}: New Resource`}</title>
            </Head>
            {newResource ?
                <AddMetricValuesForm resource={newResource} />:
                <NewResourceForm setNewResource={setNewResource} token={token}/>
            }
        </>
    );
}

export default NewResource;