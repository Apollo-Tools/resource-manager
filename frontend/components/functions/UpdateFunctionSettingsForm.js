import {Button, Divider, Form, InputNumber, Space, Switch, Typography} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {updateFunctionSettings} from '../../lib/api/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/api/CodeEditorService';
import TooltipIcon from '../misc/TooltipIcon';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';


const UpdateFunctionSettingsForm = ({func, reloadFunction, setError}) => {
  const [form] = Form.useForm();
  const {payload, token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState();
  const [canEdit, setCanEdit] = useState(false);
  const [isModified, setModified] = useState(false);

  useEffect(() => {
    if (func != null) {
      setCanEdit(payload?.account_id === func.created_by.account_id);
      resetFields();
    }
  }, [func]);

  const resetFields = () => {
    form.setFieldsValue({
      code: func.code,
      timeout: func.timeout_seconds,
      memorySize: func.memory_megabytes,
      isPublic: func.is_public,
    });
    setModified(false);
  };

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await updateFunctionSettings(func.function_id, values.code, values.timeout, values.memorySize, values.isPublic,
          token, setLoading, setError)
          .then(() => reloadFunction().then(() => setModified(false)));
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const checkCodeIsModified = () => {
    const code = form.getFieldValue('code');
    if (func === null) {
      return false;
    }
    return code !== func.code;
  };

  return (
    <>
      <Form
        name="updateFunctionSettingsForm"
        form={form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
        onChange={() => setModified(true)}
        disabled={!canEdit}
      >
        <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
          <TextDataDisplay label="Function Type" value={func.function_type.name} className="col-span-6"/>
          <TextDataDisplay label="Name" value={func.name} className="col-span-6"/>
          <TextDataDisplay label="Runtime" value={func.runtime.name} className="col-span-6"/>
          <TextDataDisplay label="Created by" value={func.created_by.username} className="col-span-6" />
          <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={func.created_at} includeTime/>}
            className="col-span-6"/>
          <TextDataDisplay label="Updated at" value={<DateFormatter dateTimestamp={func.updated_at} includeTime/>}
            className="col-span-6"/>
          <Divider className="col-span-full"/>
          <Form.Item
            label={<>
                Timeout
              <TooltipIcon text="the timeout of the function in seconds" />
            </>}
            name="timeout"
            rules={[
              {
                required: true,
                message: 'Please input the function timeout!',
              },
            ]}
            className="col-span-6"
          >
            <InputNumber className="w-40" controls={false} min={5} max={900} precision={0} addonAfter="s" />
          </Form.Item>

          <Form.Item
            label={<>
                Memory
              <TooltipIcon text="the memory size of the function in megabytes (only used by AWS Lambda)" />
            </>}
            name="memorySize"
            rules={[
              {
                required: true,
                message: 'Please input the memory size!',
              },
            ]}
            className="col-span-6"
          >
            <InputNumber className="w-40" controls={false} min={128} max={10240} precision={0} addonAfter="MB" />
          </Form.Item>
          <Form.Item
            label={<>
                Is Public
              <TooltipIcon text="share function with all users" />
            </>}
            name="isPublic"
            valuePropName={'checked'}
            className="col-span-6"
          >
            <Switch checkedChildren="true" unCheckedChildren="false" onChange={() => {
              setModified(true);
            }}/>
          </Form.Item>

          {!func.is_file && (
            <Form.Item
              label={<Typography.Title level={5} className="mt-3">Code</Typography.Title>}
              name="code"
              rules={[
                {
                  required: true,
                  message: 'Please input the function code!',
                },
              ]}
              className="col-span-full"
            >
              <CodeMirror
                height="500px"
                extensions={getEditorExtension(func.runtime.name)}
                onChange={() => setModified(checkCodeIsModified())}
                editable={canEdit}
              />
            </Form.Item>
          )}
        </div>
        {canEdit && <Form.Item className="col-span-full">
          <Space>
            <Button type="primary" htmlType="submit" disabled={!isModified}>
                      Update
            </Button>
            <Button type="default" onClick={() => resetFields()} disabled={!isModified}>
                Reset
            </Button>
          </Space>
        </Form.Item>}
      </Form>
    </>
  );
};

UpdateFunctionSettingsForm.propTypes = {
  func: PropTypes.object.isRequired,
  reloadFunction: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default UpdateFunctionSettingsForm;
