import {Button, Checkbox, Form, Select} from 'antd';
import {createResource} from '../lib/ResourceService';
import {useEffect, useState} from 'react';
import {listResourceTypes} from '../lib/ResourceTypeService';
import {useAuth} from '../lib/AuthenticationProvider';
import PropTypes from 'prop-types';


const NewResourceForm = ({setNewResource}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [resourceTypes, setResourceTypes] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResourceTypes(token, setResourceTypes, setError);
    }
  }, []);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createResource(values.resourceType, values.isSelfManaged, token, setNewResource, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <>
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
          rules={[
            {
              required: true,
              message: 'Missing resource type',
            },
          ]}
        >
          <Select className="w-40">
            {resourceTypes.map((resourceType) => {
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
    </>
  );
};

NewResourceForm.propTypes = {
  setNewResource: PropTypes.func.isRequired,
};

export default NewResourceForm;
