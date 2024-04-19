import {useState} from 'react';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {Button, Form, Input, Typography, App} from 'antd';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import Link from 'next/link';
import {signUp} from '../../lib/api/AccountService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {successNotification} from '../../lib/misc/NotificationProvider';

const {Title} = Typography;

const NewAccount = ({setError}) => {
  const {notification} = App.useApp();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);
  const [form] = Form.useForm();

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await signUp(values.username, values.password, token, setLoading, setError)
          .then((result) => {
            if (result) {
              successNotification(notification, 'Account has been created');
              form.resetFields();
            }
          });
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const validateConfirmPassword = (_, value) => {
    const {password} = form.getFieldsValue();
    if (value && value !== password) {
      return Promise.reject(new Error('The two passwords do not match.'));
    }
    return Promise.resolve();
  };

  return (
    <div className="card container md:w-1/2 max-w-xl pt-5 pl-10 pr-10 pb-1.5 border-2">
      <Head>
        <title>{`${siteTitle}: Signup`}</title>
      </Head>
      <Title level={3} className="text-center m-0">Create a new Account</Title>
      <Form
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        layout="vertical"
        form={form}
      >
        <Form.Item
          label="Username"
          name="username"
          rules={[
            {
              required: true,
              message: 'Please provide a new username!',
            },
            {
              pattern: /^[a-z_0-9]+$/,
              message: 'Only lower case letters, digits and underscore are allowed.',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Username"/>
        </Form.Item>
        <Form.Item
          label="Password"
          name="password"
          rules={[
            {
              required: true,
              message: 'Please provide a password!',
            },
            {
              pattern: /^(?=.*\d)(?=.*\p{Ll})(?=.*\p{Lu})(?=.*\p{P})(?!.*\s).{8,512}$/u,
              message: 'The password does not meet the security criteria!',
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item
          label="Confirm Password"
          name="confirmPassword"
          hasFeedback
          dependencies={['password']}
          rules={[
            {
              required: true,
              message: 'Please confirm the password!',
            },
            {
              validator: validateConfirmPassword,
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item>
          <Link href={`/accounts/accounts`} className="float-left">
            <Button type="default">
              Cancel
            </Button>
          </Link>
          <Button type="primary" htmlType="submit" className="float-right">
            Create
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

NewAccount.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewAccount;
