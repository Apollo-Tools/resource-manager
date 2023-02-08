import {Button, Checkbox, Form, Input, Select, Space} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {updateResource} from '../../lib/ResourceService';
import PropTypes from 'prop-types';
import {listResourceTypes} from '../../lib/ResourceTypeService';


const UpdateResourceForm = ({resource, reloadResource}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isModified, setModified] = useState(false);
  const [error, setError] = useState(false);
  const [resourceTypes, setResourceTypes] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResourceTypes(token, setResourceTypes, setError)
          .then(() => setResourceTypes((prevTypes) =>
            prevTypes.sort((a, b) => a.resource_type.localeCompare(b.resource_type)),
          ));
    }
  }, []);

  useEffect(() => {
    if (resource != null && resourceTypes.length > 0) {
      resetFields();
    }
  }, [resource, resourceTypes]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

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
      isSelfManaged: resource.is_self_managed,
      region: resource.region.name,
      provider: resource.region.resource_provider.provider,
    });
    setModified(false);
  };

  const checkIsModified = () => {
    const isSelfManaged = form.getFieldValue('isSelfManaged');
    const resourceType = form.getFieldValue('resourceType');
    const region = form.getFieldValue('region');

    console.log('check ' + isModified + isSelfManaged + resourceType);
    if (resource === null) {
      return false;
    }
    return isSelfManaged !== resource.is_self_managed ||
      resourceType !== resource.resource_type?.type_id ||
      region !== resource.region.region_id;
  };

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
          label="Resource Type"
          name="resourceType"
        >
          <Select className="w-40" onChange={() => setModified(checkIsModified())}>
            {resourceTypes.map((resourceType) => {
              return (
                <Select.Option value={resourceType.type_id} key={resourceType.type_id} >
                  {resourceType.resource_type}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        <Form.Item
          label="Provider"
          name="provider"
        >
          <Input className="text-black bg-blank w-40" onChange={() => setModified(checkIsModified())} disabled />
        </Form.Item>

        <Form.Item
          label="Region"
          name="region"
        >
          <Input className="text-black bg-blank w-40" onChange={() => setModified(checkIsModified())} disabled />
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
};

UpdateResourceForm.propTypes = {
  resource: PropTypes.object.isRequired,
  reloadResource: PropTypes.func.isRequired,
};

export default UpdateResourceForm;
