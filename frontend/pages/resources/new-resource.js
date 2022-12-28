import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { useEffect, useState } from 'react';
import { Result, Button } from 'antd';
import { SmileOutlined } from '@ant-design/icons';
import NewResourceForm from '../../components/NewResourceForm';
import AddMetricValuesForm from '../../components/AddMetricValuesForm';


const NewResource = () => {
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
            <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
                {finished ?
                    <Result
                        icon={<SmileOutlined />}
                        title="The resource has been created!"
                        extra={<Button type="primary" onClick={onClickRestart}>Restart</Button>}
                    />:
                    (newResource ?
                    <AddMetricValuesForm resource={newResource} setFinished={setFinished} isSkipable />:
                    <NewResourceForm setNewResource={setNewResource} />)
                }
            </div>
        </>
    );
}

export default NewResource;