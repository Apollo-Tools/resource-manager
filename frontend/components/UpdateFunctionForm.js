import {Button, Form, Input, Select, Space} from 'antd';
import {useAuth} from '../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {updateFunction} from '../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {python} from '@codemirror/lang-python';

function getEditorExtension(runtimeName) {
  if (runtimeName.toLowerCase().startsWith('python')) {
    return python();
  }
  return null;
}


const UpdateFunctionForm = ({func, runtimes, reloadFunction}) => {
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
    if (func != null && runtimes.length > 0) {
      resetFields();
    }
  }, [func, runtimes]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await updateFunction(func.function_id, values.name, values.runtime, values.code, token, setError)
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
      runtime: func.runtime.runtime_id,
      code: func.code,
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

  const getCurrentRuntime = () => {
    return runtimes
        .filter((runtime) => runtime.runtime_id === form.getFieldValue('runtime'))[0];
  };

  const onChangeRuntime = () => {
    const extension = getEditorExtension(getCurrentRuntime().name);
    setEditorExtensions(extension != null ? [extension] : []);
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
          label="Runtime:"
          name="runtime"
        >
          <Select className="w-40" onChange={() => {
            setModified(checkIsModified());
            onChangeRuntime();
          }}>
            {runtimes.map((runtime) => {
              return (
                <Select.Option value={runtime.runtime_id} key={runtime.runtime_id} >
                  {runtime.name}
                </Select.Option>
              );
            })}
          </Select>
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
            value="console.log('hello world!');"
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
  runtimes: PropTypes.arrayOf(PropTypes.object).isRequired,
  reloadFunction: PropTypes.func.isRequired,
};

export default UpdateFunctionForm;
