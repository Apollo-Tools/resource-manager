import {Button, Divider, Form, Input, InputNumber, Select, Switch, Upload} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {getRuntimeTemplate, listRuntimes} from '../../lib/api/RuntimeService';
import {createFunctionCode, createFunctionUpload} from '../../lib/api/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/api/CodeEditorService';
import {PlusOutlined} from '@ant-design/icons';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/api/FormValidationRules';
import {listFunctionTypes} from '../../lib/api/FunctionTypeService';
import TooltipIcon from '../misc/TooltipIcon';
import {updateLoadingState} from '../../lib/misc/LoadingUtil';
import LoadingSpinner from '../misc/LoadingSpinner';


const NewFunctionFrom = ({setNewFunction, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(
      {
        listRuntimes: true,
        listFunctionTypes: true,
        createFunction: false,
        getRuntimeTemplate: true,
      });
  const [functionTypes, setFunctionTypes] = useState([]);
  const [runtimes, setRuntimes] = useState([]);
  const [editorExtensions, setEditorExtensions] = useState([]);
  const [functionTemplate, setFunctionTemplate] = useState('');
  const [isFile, setIsFile] = useState(true);
  const [selectedRuntime, setSelectedRuntime] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listRuntimes(token, setRuntimes, updateLoadingState('listRuntimes', setLoading), setError);
      void listFunctionTypes(token, setFunctionTypes, updateLoadingState('listFunctionTypes', setLoading),
          setError);
    }
  }, []);

  useEffect(() => {
    if (functionTemplate != null) {
      form.setFieldValue('code', functionTemplate.template);
    }
  }, [functionTemplate]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      if (values.isFile) {
        await createFunctionUpload(values.name, values.functionType, values.runtime, values.upload.originFileObj,
            values.timeout, values.memorySize, values.isPublic, token, setNewFunction,
            updateLoadingState('createFunction', setLoading), setError);
      } else {
        await createFunctionCode(values.name, values.functionType, values.runtime, values.code, values.timeout,
            values.memorySize, values.isPublic, token, setNewFunction,
            updateLoadingState('createFunction', setLoading), setError);
      }
    }
  };

  const getCurrentRuntime = () => {
    return runtimes
        .filter((runtime) => runtime.runtime_id === form.getFieldValue('runtime'))[0];
  };

  const onChangeIsFile = async (value) => {
    setIsFile(value);
    if (!value) {
      const currentRuntime = getCurrentRuntime();
      const extension = getEditorExtension(currentRuntime.name);
      setEditorExtensions(extension != null ? [extension] : []);
      if (!checkTokenExpired()) {
        await getRuntimeTemplate(currentRuntime.runtime_id, token, setFunctionTemplate,
            updateLoadingState('getRuntimeTemplate', setLoading), setError);
      }
    }
  };

  const onRuntimeChange = (value) => {
    setSelectedRuntime(value);
    setIsFile(true);
    form.setFieldValue(['isFile'], true);
  };

  if (isLoading['listRuntimes'] || isLoading['listFunctionTypes']) {
    return (<LoadingSpinner isCard={false}/>);
  }

  return (
    <>
      <Form
        name="newFunctionForm"
        form={form}
        onFinish={onFinish}
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
            label={<>
                Is Public
              <TooltipIcon text="share function with all users" />
            </>}
            name="isPublic"
            valuePropName={'checked'}
            initialValue={false}
            className="col-span-6"
          >
            <Switch checkedChildren="true" unCheckedChildren="false"/>
          </Form.Item>
          <Divider className="col-span-full"/>
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
                  <Select.Option value={runtime.runtime_id} key={runtime.runtime_id}>
                    {runtime.name}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
          {selectedRuntime != null && <>
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
              className="col-span-full mb-0"
            >
              <Switch
                checked={isFile}
                onChange={async (value) => await onChangeIsFile(value)}
                checkedChildren="true"
                unCheckedChildren="false"
              />
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
                      if (value == null || !value.originFileObj.name.endsWith('.zip')) {
                        return Promise.reject(new Error('Invalid file type. Make sure to upload a zip archive'));
                      } else if (value.status !== 'done') {
                        return Promise.reject(new Error('File upload in progress / No file uploaded yet'));
                      }
                      return Promise.resolve();
                    },
                  },
                ]}
                getValueFromEvent={({file}) => file}
                className="col-span-full"
                valuePropName='file'
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
                className="col-span-full"
              >
                {isLoading['getRuntimeTemplate'] ?
                  <LoadingSpinner isCard={false}/> :
                  <CodeMirror height="500px" extensions={editorExtensions}/> }
              </Form.Item>
            }
          </>}
        </div>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={isLoading['createFunction']}>
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewFunctionFrom.propTypes = {
  setNewFunction: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default NewFunctionFrom;
