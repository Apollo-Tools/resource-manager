import {useEffect, useState} from 'react';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {Button, Form, Input, Typography} from 'antd';
import {getLogin} from '../../lib/api/AccountService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {useRouter} from 'next/router';

const {Title} = Typography;

const login = ({setError}) => {
  const {isAuthenticated, loginUser} = useAuth();
  const [isLoading, setLoading] = useState(false);
  const router = useRouter();

  useEffect(() => {
    const checkAuthenticationState = async () => {
      if (isAuthenticated) {
        await router.replace('/');
      }
    };
    void checkAuthenticationState();
  }, []);

  const onFinish = async (values) => {
    await getLogin(values.username, values.password, loginUser, setLoading, setError);
  };

  return (
    <div className="card container md:w-1/2 max-w-xl pt-5 pl-10 pr-10 pb-1.5 border-2 mt-10">
      <Head>
        <title>{`${siteTitle}: Login`}</title>
      </Head>
      <div className="">
        <Title level={3} className="text-center m-0">Welcome Back</Title>
        <Title level={5} className="text-center m-0">Sign in to your account!</Title>
      </div>
      <Form
        onFinish={onFinish}
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
          <Button type="primary" htmlType="submit" className="float-right" loading={isLoading}>
            Login
          </Button>
        </Form.Item>
      </Form>
    </div>
  );
};

export default login;
