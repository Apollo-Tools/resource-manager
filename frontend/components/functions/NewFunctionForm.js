import {Button, Form, Input, Select, Switch, Upload} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {getRuntimeTemplate, listRuntimes} from '../../lib/RuntimeService';
import {createFunctionCode, createFunctionUpload} from '../../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/CodeEditorService';
import {PlusOutlined} from '@ant-design/icons';


const NewFunctionFrom = ({setNewFunction}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [runtimes, setRuntimes] = useState([]);
  const [editorExtensions, setEditorExtensions] = useState([]);
  const [functionTemplate, setFunctionTemplate] = useState('');
  const [isFile, setIsFile] = useState(true);
  const [selectedRuntime, setSelectedRuntime] = useState();

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
      console.log(values);
      if (values.isFile) {
        await createFunctionUpload(values.runtime, values.name, values.upload, token, setNewFunction, setError);
      } else {
        await createFunctionCode(values.runtime, values.name, values.code, token, setNewFunction, setError);
      }
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const getCurrentRuntime = () => {
    return runtimes
        .filter((runtime) => runtime.runtime_id === form.getFieldValue('runtime'))[0];
  };

  const onChangeIsFile = (value) => {
    console.log(value);
    setIsFile(value);
    if (!value) {
      const currentRuntime = getCurrentRuntime();
      const extension = getEditorExtension(currentRuntime.name);
      setEditorExtensions(extension != null ? [extension] : []);
      if (!checkTokenExpired()) {
        getRuntimeTemplate(currentRuntime.runtime_id, token, setFunctionTemplate, setError);
      }
    }
  };

  const onRuntimeChange = (value) => {
    setSelectedRuntime(value);
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
          <Select className="w-40" onChange={onRuntimeChange}>
            {runtimes.map((runtime) => {
              return (
                <Select.Option value={runtime.runtime_id} key={runtime.runtime_id} >
                  {runtime.name}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>


        {selectedRuntime != null && (<>
          <Form.Item
            label="Editor / Upload"
            name="isFile"
            hidden={getCurrentRuntime().name !== 'python3.8'}
            shouldUpdate={(prevValues, curValues) => {
              console.log(prevValues.runtime);
              return prevValues.runtime !== curValues.runtime;
            }
            }
            valuePropName={'checked'}
            initialValue={isFile}
          >
            <Switch checked={isFile} onChange={onChangeIsFile}/>
          </Form.Item>
          {isFile ?
            <Form.Item
              label="Upload"
              name="upload"
              rules={[
                {
                  required: isFile,
                  message: 'Please upload a .zip file that contains the function code!',
                },
                {
                  validator: () => {
                    const value = form.getFieldValue(['upload']) ?? {};
                    console.log(value);
                    if (!value.name.endsWith('.zip')) {
                      return Promise.reject(new Error('Invalid file type. Make sure to upload a zip archive'));
                    }
                    return Promise.resolve();
                  },
                },
              ]}
              getValueFromEvent={({file}) => file.originFileObj}
            >
              <Upload accept=".zip" maxCount={1} multiple={false} listType="picture-card" showUploadList={{showPreviewIcon: false}}>
                <div>
                  <PlusOutlined />
                  <div
                    style={{
                      marginTop: 8,
                    }}
                  >
                    Upload
                  </div>
                </div>
              </Upload>
            </Form.Item> :
            <Form.Item
              label="Code"
              name="code"
              rules={[
                {
                  required: !isFile,
                  message: 'Please input the function code!',
                },
              ]}
            >
              <CodeMirror height="500px" extensions={editorExtensions}/>
            </Form.Item>
          }
        </>)}
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
