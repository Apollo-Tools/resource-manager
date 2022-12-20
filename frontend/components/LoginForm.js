import { useEffect, useState } from 'react';
import { LockOutlined, UserOutlined } from '@ant-design/icons';
import { Button, Form, Input, Typography, message } from 'antd';
import {login} from '../lib/AccountService';
import { useAuth } from '../lib/AuthenticationProvider';
import { siteTitle } from './Sidebar';
import Head from 'next/head';

const {Title} = Typography;

const loginForm = () => {
    const {setNewToken} = useAuth();
    const [error, setError] = useState(false);
    const [messageApi, contextHolder] = message.useMessage();

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
        await login(values.username, values.password, setNewToken, setError);
    }

    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo)
    }

    return (
        <div className="card container md:w-1/2 max-w-xl p-10 border-2">
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
            >
                <Form.Item
                    label="Username"
                    name="username"
                    rules={[
                        {
                            required: true,
                            message: 'Please input your username!',
                        }
                    ]}
                    >
                    <Input  prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Username"/>
                </Form.Item>
                <Form.Item
                    label="Password"
                    name="password"
                    rules={[
                        {
                            required: true,
                            message: 'Please input your password!',
                        }
                    ]}
                >
                    <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
                </Form.Item>
                <Form.Item className="float-right">
                    <Button type="primary" htmlType="submit">
                        Login
                    </Button>
                </Form.Item>
            </Form>
        </div>
    );
}

export default loginForm;