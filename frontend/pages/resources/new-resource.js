import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { useEffect, useState } from 'react';
import { useAuth } from '../../lib/AuthenticationProvider';
import { Result, Button } from 'antd';
import { SmileOutlined } from '@ant-design/icons';
import NewResourceForm from '../../components/NewResourceForm';
import AddMetricValuesForm from '../../components/AddMetricValuesForm';


const NewResource = () => {
    const {token} = useAuth();
    const [newResource, setNewResource] = useState(null);
    const [finished, setFinished] = useState(false);

    useEffect(() => {
        if (newResource != null) {
            console.log("new resource " + newResource);
        }
    }, [newResource])

    const onClickRestart = () => {
        setNewResource(null);
        setFinished(false);
    }

    return (
        <>
            <Head>
                <title>{`${siteTitle}: New Resource`}</title>
            </Head>
            {finished ?
                <Result
                    icon={<SmileOutlined />}
                    title="The resource has been created!"
                    extra={<Button type="primary" onClick={onClickRestart}>Restart</Button>}
                />:
                (newResource ?
                <AddMetricValuesForm resource={newResource} setFinished={setFinished}/>:
                <NewResourceForm setNewResource={setNewResource} token={token}/>)
            }
        </>
    );
}

export default NewResource;