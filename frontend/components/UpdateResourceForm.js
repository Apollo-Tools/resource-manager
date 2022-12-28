import { Button, Checkbox, Form, Select, Space } from 'antd';
import { useAuth } from '../lib/AuthenticationProvider';
import { useEffect, useState } from 'react';
import { updateResource } from '../lib/ResourceService';


const UpdateResourceForm = ({resource, resourceTypes, reloadResource}) => {
    const [form] = Form.useForm();
    const {token, checkTokenExpired} = useAuth();
    const [isModified, setModified] = useState(false);
    const [error, setError] = useState(false);

    useEffect(() => {
        if (resource != null && resourceTypes.length > 0) {
            resetFields();
        }
    }, [resource, resourceTypes]);

    const onFinish = async (values) => {
        if (!checkTokenExpired()) {
            await updateResource(resource.resource_id, values.resourceType, values.isSelfManaged, token, setError)
                .then(() => reloadResource())
                .then(() => setModified(false));
            console.log(values);
        }
    };
    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

    const resetFields = () => {
        form.setFieldsValue({
            resourceType: resource.resource_type.type_id,
            isSelfManaged: resource.is_self_managed
        });
    }

    const checkIsModified = () => {
        const isSelfManaged = form.getFieldValue("isSelfManaged");
        const resourceType = form.getFieldValue("resourceType");

        console.log("check " + isModified + isSelfManaged + resourceType);
        if (resource === null) {
            return false;
        }
        return isSelfManaged !== resource.is_self_managed || resourceType !== resource.resource_type?.type_id;
    }

    return (
        <>
            <Form
                name="resource-details"
                form={form}
                onFinish={onFinish}
                onFinishFailed={onFinishFailed}
                autoComplete="off"
                layout="vertical"
            >
                <Form.Item
                    label="Is self managed?"
                    name="isSelfManaged"
                    valuePropName="checked"
                >
                    <Checkbox defaultChecked={false} onChange={() => setModified(checkIsModified())}/>
                </Form.Item>

                <Form.Item
                    label="Resource Type:"
                    name="resourceType"
                >
                    <Select className="w-40" onChange={() => setModified(checkIsModified())}>
                        {resourceTypes.map(resourceType => {
                            return (
                                <Select.Option value={resourceType.type_id} key={resourceType.type_id} >
                                    {resourceType.resource_type}
                                </Select.Option>
                            );
                        })}
                    </Select>
                </Form.Item>

                <Form.Item>
                    <Space>
                        <Button type="primary" htmlType="submit" disabled={!isModified}>
                            Update
                        </Button>
                        <Button type="default" onClick={() => resetFields()} disabled={!isModified}>
                            Reset
                        </Button>
                    </Space>
                </Form.Item>
            </Form>
        </>
    );
}

export default UpdateResourceForm;