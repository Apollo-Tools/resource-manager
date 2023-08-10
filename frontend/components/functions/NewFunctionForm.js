import { Button, Form, Input, InputNumber, Select, Switch, Upload } from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {getRuntimeTemplate, listRuntimes} from '../../lib/RuntimeService';
import {createFunctionCode, createFunctionUpload} from '../../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/CodeEditorService';
import {PlusOutlined} from '@ant-design/icons';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/FormValidationRules';
import { listFunctionTypes } from '../../lib/FunctionTypeService';
import TooltipIcon from '../misc/TooltipIcon';


const NewFunctionFrom = ({setNewFunction}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [functionTypes, setFunctionTypes] = useState([]);
  const [runtimes, setRuntimes] = useState([]);
  const [editorExtensions, setEditorExtensions] = useState([]);
  const [functionTemplate, setFunctionTemplate] = useState('');
  const [isFile, setIsFile] = useState(true);
  const [selectedRuntime, setSelectedRuntime] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listRuntimes(token, setRuntimes, setError);
      listFunctionTypes(token, setFunctionTypes, setError);
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
      if (values.isFile) {
        await createFunctionUpload(values.name, values.functionType, values.runtime, values.upload.originFileObj, values.timeout,
            values.memorySize, token, setNewFunction, setError);
      } else {
        await createFunctionCode(values.name, values.functionType, values.runtime, values.code, values.timeout,
            values.memorySize, token, setNewFunction, setError);
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
        <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
          <Form.Item
              label="Function Type"
              name="functionType"
              rules={[
                {
                  required: true,
                  message: 'Please input a function type!',
                },
              ]}
              className="col-span-6"
          >
            <Select className="w-40">
              {functionTypes.map((functionType) => {
                return (
                    <Select.Option value={functionType.artifact_type_id} key={functionType.artifact_type_id} >
                      {functionType.name}
                    </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
          <Form.Item
            label="Name"
            name="name"
            rules={[nameValidationRule, nameRegexValidationRule]}
            className="col-span-6"
          >
            <Input className="w-40" />
          </Form.Item>

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
              initialValue={60}
              className="col-span-6"
          >
            <InputNumber className="w-40" controls={false} min={5} max={900} precision={0} addonAfter="s"/>
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
              initialValue={128}
              className="col-span-6"
          >
            <InputNumber className="w-40" controls={false} min={128} max={10240} precision={0} addonAfter="MB"/>
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
              className="col-span-6"
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
                return prevValues.runtime !== curValues.runtime;
              }
              }
              valuePropName={'checked'}
              initialValue={isFile}
              className="lg:col-span-12 col-span-6 mb-0"
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
                    validator: (_, value) => {
                      if (!value.originFileObj.name.endsWith('.zip')) {
                        return Promise.reject(new Error('Invalid file type. Make sure to upload a zip archive'));
                      } else if (value.status !== 'done') {
                        return Promise.reject(new Error('File upload in progress / No file uploaded yet'));
                      }

                      return Promise.resolve();
                    },
                  },
                ]}
                getValueFromEvent={({file}) => file}
                className="lg:col-span-12 col-span-6"
              >
                <Upload
                  accept=".zip"
                  maxCount={1}
                  multiple={false}
                  listType="picture-card"
                  showUploadList={{showPreviewIcon: false}}
                  beforeUpload={() => true}
                >
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
                className="lg:col-span-12 col-span-6"
              >
                <CodeMirror height="500px" extensions={editorExtensions}/>
              </Form.Item>
            }
          </>)}
        </div>
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
