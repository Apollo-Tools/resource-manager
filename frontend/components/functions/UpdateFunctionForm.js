import {Button, Form, Input, Space} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {updateFunction} from '../../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/CodeEditorService';

const UpdateFunctionForm = ({func, reloadFunction}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isModified, setModified] = useState(false);
  const [error, setError] = useState(false);
  const [editorExtensions, setEditorExtensions] = useState([]);

  useEffect(() => {
    if (func != null && Object.hasOwn(func, 'runtime')) {
      const extension = getEditorExtension(func.runtime.name);
      setEditorExtensions(extension != null ? [extension] : []);
    }
  }, [func]);

  useEffect(() => {
    if (func != null) {
      resetFields();
    }
  }, [func]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await updateFunction(func.function_id, values.name, values.code, token, setError)
          .then(() => reloadFunction())
          .then(() => setModified(false));
      console.log(values);
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const resetFields = () => {
    form.setFieldsValue({
      name: func.name,
      code: func.code,
      runtime: func.runtime.name,
    });
    const extension = getEditorExtension(func.runtime.name);
    setEditorExtensions(extension != null ? [extension] : []);
  };

  const checkIsModified = () => {
    const name = form.getFieldValue('name');
    const runtime = form.getFieldValue('runtime');
    const code = form.getFieldValue('code');

    console.log('check ' + isModified + name + runtime + code);
    if (func === null) {
      return false;
    }
    return name !== func.name || runtime !== func.runtime?.runtime_id || runtime !== func.code;
  };

  return (
    <>
      <Form
        name="func-details"
        form={form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <Form.Item
          label="Name"
          name="name"
          rules={[
            {
              required: true,
              message: 'Please input a name!',
            },
          ]}
        >
          <Input className="w-40" onChange={() => setModified(checkIsModified())}/>
        </Form.Item>

        <Form.Item
          label="Runtime"
          name="runtime"
        >
          <Input className="text-black bg-blank w-40" onChange={() => setModified(checkIsModified())} disabled/>
        </Form.Item>

        <Form.Item
          label="Code"
          name="code"
          rules={[
            {
              required: true,
              message: 'Please input the function code!',
            },
          ]}
        >
          <CodeMirror
            height="200px"
            extensions={editorExtensions}
            onChange={() => setModified(checkIsModified())}
          />
        </Form.Item>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" disabled={!isModified}>
              Update
            </Button>
            <Button type="default" onClick={() => resetFields()} disabled={!isModified}>
              Reset
            </Button>
          </Space>
        </Form.Item>
      </Form>
    </>
  );
};

UpdateFunctionForm.propTypes = {
  func: PropTypes.object.isRequired,
  reloadFunction: PropTypes.func.isRequired,
};

export default UpdateFunctionForm;
