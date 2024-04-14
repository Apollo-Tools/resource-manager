import {useState} from 'react';
import {LockOutlined} from '@ant-design/icons';
import {App, Button, Form, Input, Space} from 'antd';
import {changePassword} from '../../lib/api/AccountService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {successNotification} from '../../lib/misc/NotificationProvider';

const ResetPasswordForm = ({setError}) => {
  const {notification} = App.useApp();
  const {token, checkTokenExpired} = useAuth();
  const [form] = Form.useForm();
  const [show, setShow] = useState(false);
  const [isLoading, setLoading] = useState(false);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await changePassword(values.oldPassword, values.newPassword, token, setLoading, setError)
          .then((result) => {
            if (result) {
              successNotification(notification, 'Password change was successful!');
              setShow(false);
              form.resetFields();
            }
          });
    }
  };

  const onClickCancel = () => {
    form.resetFields();
    setShow(false);
  };

  return (
    <div>
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

ResetPasswordForm.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default ResetPasswordForm;
