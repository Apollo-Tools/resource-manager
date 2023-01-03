import {useEffect, useState} from 'react';
import {LockOutlined} from '@ant-design/icons';
import {Button, Form, Input, message, Space} from 'antd';
import {changePassword} from '../lib/AccountService';
import {useAuth} from '../lib/AuthenticationProvider';

const ResetPasswordForm = () => {
  const {token, checkTokenExpired} = useAuth();
  const [form] = Form.useForm();
  const [response, setResponse] = useState();
  const [show, setShow] = useState(false);
  const [error, setError] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();

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
    if (response && response.ok) {
      messageApi.open({
        type: 'success',
        content: 'Password change was successful!',
      });
      setShow(false);
      form.resetFields();
    } else if (response && !response.ok) {
      messageApi.open({
        type: 'error',
        content: 'Old password was wrong!',
      });
      setError(false);
    }
  }, [response]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await changePassword(values.oldPassword, values.newPassword, token, setResponse, setError)
          .then(() => {
            console.log(error);
          });
    }
  };

  const onClickCancel = () => {
    form.resetFields();
    setShow(false);
  };

  return (
    <div>
      {contextHolder}
      { show ?
        <>
          <Form
            form={form}
            onFinish={onFinish}
            layout="vertical"
            className="max-w-sm"
          >
            <Form.Item
              label="Old Password"
              name="oldPassword"
              rules={[
                {
                  required: true,
                  message: 'Please input your old password!',
                },
              ]}
            >
              <Input.Password prefix={<LockOutlined className="site-form-item-icon" />} />
            </Form.Item>
            <Form.Item
              label="New Password"
              name="newPassword"
              rules={[
                {
                  required: true,
                  message: 'Please input your new password!',
                },
                {
                  pattern: /^(?=.*\d)(?=.*\p{Ll})(?=.*\p{Lu})(?=.*\p{P})(?!.*\s).{8,512}$/u,
                  message: 'The password does not meet the security criteria!',
                },
              ]}
            >
              <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
            </Form.Item>
            <Form.Item shouldUpdate>
              <Space size="large" className="float-right">
                <Button
                  type="primary"
                  htmlType="submit"
                >
                  Submit
                </Button>
                <Button type="default" onClick={onClickCancel}>Cancel</Button>
              </Space>
            </Form.Item>
          </Form>
        </>:
        <Button type="primary" onClick={() => setShow(true)}>Reset</Button>
      }
    </div>
  );
};

export default ResetPasswordForm;
