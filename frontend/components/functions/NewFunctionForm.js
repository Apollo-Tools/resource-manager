import {Button, Form, Input, Select} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {getRuntimeTemplate, listRuntimes} from '../../lib/RuntimeService';
import {createFunction} from '../../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/CodeEditorService';


const NewFunctionFrom = ({setNewFunction}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [runtimes, setRuntimes] = useState([]);
  const [editorExtensions, setEditorExtensions] = useState([]);
  const [functionTemplate, setFunctionTemplate] = useState('');

  useEffect(() => {
    if (!checkTokenExpired()) {
      listRuntimes(token, setRuntimes, setError);
    }
  }, []);

  useEffect(() => {
    if (functionTemplate != null) {
      form.setFieldValue('code', functionTemplate);
    }
  }, [functionTemplate]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createFunction(values.runtime, values.name, values.code, token, setNewFunction, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const getCurrentRuntime = () => {
    return runtimes
        .filter((runtime) => runtime.runtime_id === form.getFieldValue('runtime'))[0];
  };

  const onChangeRuntime = () => {
    const currentRuntime = getCurrentRuntime();
    console.log(currentRuntime);
    const extension = getEditorExtension(currentRuntime.name);
    setEditorExtensions(extension != null ? [extension] : []);
    if (!checkTokenExpired()) {
      getRuntimeTemplate(currentRuntime.runtime_id, token, setFunctionTemplate, setError);
    }
  };

  return (
    <>
      <Form
        name="newFunctionForm"
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
          <Input className="w-40" />
        </Form.Item>

        <Form.Item
          label="Runtime"
          name="runtime"
          rules={[
            {
              required: true,
              message: 'Please input a runtime!',
            },
          ]}
        >
          <Select className="w-40" onChange={() => onChangeRuntime()}>
            {runtimes.map((runtime) => {
              return (
                <Select.Option value={runtime.runtime_id} key={runtime.runtime_id} >
                  {runtime.name}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        {form.getFieldValue('runtime') != null && (<Form.Item
          label="Code"
          name="code"
          rules={[
            {
              required: true,
              message: 'Please input the function code!',
            },
          ]}
        >
          <CodeMirror height="500px" extensions={editorExtensions}/>
        </Form.Item>)}

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewFunctionFrom.propTypes = {
  setNewFunction: PropTypes.func.isRequired,
};

export default NewFunctionFrom;
