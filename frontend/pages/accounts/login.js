import {useEffect, useState} from 'react';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {Button, Form, Input, Typography, message} from 'antd';
import {getLogin} from '../../lib/AccountService';
import {useAuth} from '../../lib/AuthenticationProvider';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {useRouter} from 'next/router';
import Link from 'next/link';

const {Title} = Typography;

const login = () => {
  const {isAuthenticated, loginUser} = useAuth();
  const [error, setError] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const router = useRouter();

  useEffect(() => {
    const checkAuthenticationState = async () => {
      if (isAuthenticated) {
        await router.replace('/');
      }
    };
    checkAuthenticationState();
  }, []);

  useEffect(() => {
    if (error) {
      messageApi.open({
        type: 'error',
        content: 'Invalid credentials!',
      });
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    await getLogin(values.username, values.password, loginUser, setError);
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <div className="card container md:w-1/2 max-w-xl pt-5 pl-10 pr-10 pb-1.5 border-2">
      <Head>
        <title>{`${siteTitle}: Login`}</title>
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
      >
        <Form.Item
          label="Username"
          name="username"
          rules={[
            {
              required: true,
              message: 'Please input your username!',
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
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item>
          <Link href={`/accounts/signup`} className="float-left">
            <Button type="link">
              Create new account
            </Button>
          </Link>
          <Button type="primary" htmlType="submit" className="float-right">
            Login
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default login;
