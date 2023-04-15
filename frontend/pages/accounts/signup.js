import {useEffect, useState} from 'react';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {Button, Form, Input, Typography, message} from 'antd';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import Link from 'next/link';
import {signUp} from '../../lib/AccountService';

const {Title} = Typography;

const signup = () => {
  const [error, setError] = useState();
  const [messageApi, contextHolder] = message.useMessage();
  const [response, setResponse] = useState();
  const [form] = Form.useForm();

  useEffect(() => {
    console.log(response);
    if (response && response.ok) {
      messageApi.open({
        type: 'success',
        content: 'Signup was successful! Navigate back to login.',
      });
      form.resetFields();
    } else if (response && !response.ok) {
      messageApi.open({
        type: 'error',
        content: 'Signup failed!',
      });
      setError(false);
    }
  }, [response]);


  useEffect(() => {
    if (error) {
      let msg = 'Signup failed';
      if (error.status === 409) {
        msg = 'Username already exists';
      }
      messageApi.open({
        type: 'error',
        content: msg,
      });
      setError(null);
    }
  }, [error]);

  const onFinish = async (values) => {
    await signUp(values.username, values.password, setResponse, setError);
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
      {contextHolder}
      <div className="mb-6">
        <Title level={3} className="text-center m-0">Resource Manager</Title>
        <Title level={4} className="text-center m-0">Apollo Tools</Title>
      </div>
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
              message: 'Please input a new username!',
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
              message: 'Please input your password!',
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
              message: 'Please confirm your password!',
            },
            {
              validator: validateConfirmPassword,
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item>
          <Link href={`/accounts/login`} className="float-left">
            <Button type="default">
              Return to login
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

export default signup;
