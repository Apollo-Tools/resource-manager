import Head from 'next/head';
import { siteTitle } from '../../components/Sidebar';
import { Form, Button, Checkbox, Select } from 'antd';
import { useEffect, useState } from 'react';
import { listResourceTypes } from '../../lib/ResourceTypeService';
import { useAuth } from '../../lib/AuthenticationProvider';
import { createResource } from '../../lib/ResourceService';


const NewResource = () => {
    const {token} = useAuth();
    const [error, setError] = useState();
    const [resourceTypes, setResourceTypes] = useState([]);
    const [resource, setNewResource] = useState();

    useEffect(() => {
        listResourceTypes(token, setResourceTypes, setError);
    }, [])

    useEffect(() => {
        if (resource !== null) {
            console.log("new resource " + resource);
        }
    }, [resource])

    const onFinish = async (values) => {
        await createResource(values.resourceType, values.isSelfManaged, token, setNewResource, setError);
    };
    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

    return (
        <>
            <Head>
                <title>{`${siteTitle}: New Resource`}</title>
            </Head>
            <div className="card container w-11/12 max-w-7xl p-10">
                <h2>New Resource</h2>
                <Form
                    name="basic"
                    onFinish={onFinish}
                    onFinishFailed={onFinishFailed}
                    autoComplete="off"
                >
                    <Form.Item
                        label="Is self managed?"
                        name="isSelfManaged"
                        valuePropName="checked"
                    >
                        <Checkbox defaultChecked={false}/>
                    </Form.Item>

                    <Form.Item
                        label="Resource Type"
                        name="resourceType"
                    >
                        <Select className="w-40">
                            {resourceTypes.map(resourceType => {
                                return (
                                    <Select.Option value={resourceType.type_id} key={resourceType.type_id}>
                                        {resourceType.resource_type}
                                    </Select.Option>
                                );
                            })}

                        </Select>
                    </Form.Item>

                    <Form.Item>
                        <Button type="primary" htmlType="submit">
                            Create
                        </Button>
                    </Form.Item>
                </Form>
            </div>
        </>
    );
}

export default NewResource;