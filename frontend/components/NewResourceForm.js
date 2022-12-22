import { Button, Checkbox, Form, Select } from 'antd';
import { createResource } from '../lib/ResourceService';
import { useEffect, useState } from 'react';
import { listResourceTypes } from '../lib/ResourceTypeService';


const NewResourceForm = ({ setNewResource, token }) => {
    const [error, setError] = useState();
    const [resourceTypes, setResourceTypes] = useState([]);

    useEffect(() => {
        listResourceTypes(token, setResourceTypes, setError);
    }, [])

    const onFinish = async (values) => {
        await createResource(values.resourceType, values.isSelfManaged, token, setNewResource, setError);
    };
    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

    return (
        <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl p-10 mt-2 mb-2">
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
    );
}

export default NewResourceForm;