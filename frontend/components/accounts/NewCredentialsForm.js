import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {Button, Form, Input, message, Select} from 'antd';
import {listResourceProviders} from '../../lib/ResourceProviderService';
import {LockOutlined} from '@ant-design/icons';
import {createCredentials} from '../../lib/CredentialsService';
import PropTypes from 'prop-types';


const NewCredentialsForm = ({excludeProviders, setFinished}) => {
  const {token, checkTokenExpired} = useAuth();
  const [resourceProviders, setResourceProviders] = useState([]);
  const [newCredentials, setNewCredentials] = useState();
  const [error, setError] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [form] = Form.useForm();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResourceProviders(token, setResourceProviders, setError);
    }
  }, []);

  useEffect(() => {
    if (error) {
      messageApi.open({
        type: 'error',
        content: 'Something went wrong!',
      });
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (newCredentials) {
      messageApi.open({
        type: 'success',
        content: 'Credentials were created successfully!',
      });
      setFinished(true);
    }
  }, [newCredentials]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createCredentials(values.resourceProvider, values.accessKey, values.secretAccessKey, values.sessionToken,
          token, setNewCredentials, setError)
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
  excludeProviders: PropTypes.bool,
  setFinished: PropTypes.func,
};

export default NewCredentialsForm;
