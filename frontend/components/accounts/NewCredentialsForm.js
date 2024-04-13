import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {Button, Form, Input, message, Select} from 'antd';
import {listResourceProviders} from '../../lib/api/ResourceProviderService';
import {LockOutlined} from '@ant-design/icons';
import {createCredentials} from '../../lib/api/CredentialsService';
import PropTypes from 'prop-types';


const NewCredentialsForm = ({excludeProviders, setFinished, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [resourceProviders, setResourceProviders] = useState([]);
  const [newCredentials, setNewCredentials] = useState();
  const [isLoading, setLoading] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [form] = Form.useForm();

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listResourceProviders(token, setResourceProviders, setError);
    }
  }, []);

  useEffect(() => {
    if (newCredentials) {
      void messageApi.open({
        type: 'success',
        content: 'Credentials were created successfully!',
      });
      setFinished(true);
    }
  }, [newCredentials]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createCredentials(values.resourceProvider, values.accessKey, values.secretAccessKey, values.sessionToken,
          token, setNewCredentials, setLoading, setError)
          .then(() => form.resetFields());
    }
  };

  return (
    <>
      {contextHolder}
      <Form
        name="credentialsForm"
        onFinish={onFinish}
        autoComplete="off"
        layout="vertical"
        form={form}
      >
        <Form.Item
          label="Resource Provider"
          name="resourceProvider"
          rules={[
            {
              required: true,
              message: 'Missing resource provider',
            },
          ]}
        >
          <Select className="w-40">
            {resourceProviders.map((resourceProvider) => {
              return (
                <Select.Option
                  value={resourceProvider.provider_id}
                  key={resourceProvider.provider_id}
                  disabled={excludeProviders.includes(resourceProvider.provider_id)}>
                  {resourceProvider.provider}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        <Form.Item
          label="Access Key"
          name="accessKey"
          rules={[
            {
              required: true,
              message: 'Please input the access key!',
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>

        <Form.Item
          label="Secret Access Key"
          name="secretAccessKey"
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>

        <Form.Item
          label="Session Token"
          name="sessionToken"
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
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

NewCredentialsForm.propTypes = {
  excludeProviders: PropTypes.arrayOf(PropTypes.number),
  setFinished: PropTypes.func,
  setError: PropTypes.func.isRequired,
};

export default NewCredentialsForm;
