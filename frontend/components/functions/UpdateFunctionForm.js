import {Button, Form, Typography, Space, Upload} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import {
  updateFunction,
  updateFunctionUpload,
} from '../../lib/FunctionService';
import CodeMirror from '@uiw/react-codemirror';
import {getEditorExtension} from '../../lib/CodeEditorService';
import TextDataDisplay from '../misc/TextDataDisplay';
import {PlusOutlined} from '@ant-design/icons';

const UpdateFunctionForm = ({func, reloadFunction}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isModified, setModified] = useState(false);
  const [fileList, setFileList] = useState([]);
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
      if (func.is_file) {
        await updateFunctionUpload(func.function_id, values.upload, token, setError);
      } else {
        await updateFunction(func.function_id, values.code, token, setError);
      }
      await reloadFunction().then(() => setModified(false))
          .then(() => setFileList([]));
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const resetFields = () => {
    form.setFieldsValue({
      code: func.code,
    });
    const extension = getEditorExtension(func.runtime.name);
    setEditorExtensions(extension != null ? [extension] : []);
    setModified(false);
  };

  const checkCodeIsModified = () => {
    const code = form.getFieldValue('code');
    if (func === null) {
      return false;
    }
    return code !== func.code;
  };

  const checkFileIsModified = (file, fileList) => {
    setFileList(fileList);
    return file.status === 'done';
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
        <Space size="large" direction="vertical" className="w-full">
          <TextDataDisplay label="Name" value={func.name} />
          <TextDataDisplay label="Runtime" value={func.runtime.name} />
          {
            func.is_file ?
              <>
                <TextDataDisplay label="Code" value="zip archive" />
                <Form.Item
                  label="Update zip archive"
                  name="upload"
                  rules={[
                    {
                      required: true,
                      message: 'Please upload a .zip file that contains the function code!',
                    },
                    {
                      validator: () => {
                        const value = form.getFieldValue(['upload']) ?? {};
                        if (!value.name.endsWith('.zip')) {
                          return Promise.reject(new Error('Invalid file type. Make sure to upload a zip archive'));
                        }
                        return Promise.resolve();
                      },
                    },
                  ]}
                  getValueFromEvent={({file}) => file.originFileObj}
                >
                  <Upload
                    accept=".zip"
                    maxCount={1}
                    multiple={false}
                    listType="picture-card"
                    showUploadList={{showPreviewIcon: false}}
                    onChange={({file, fileList}) => setModified(checkFileIsModified(file, fileList))}
                    fileList={fileList}
                    action={'https://run.mocky.io/v3/0039e1d4-7e0a-4a00-a89c-6606e83fae2b'}
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
                </Form.Item>
              </> :
              <Form.Item
                label={<Typography.Title level={5} className="mt-3">Code</Typography.Title>}
                name="code"
                rules={[
                  {
                    required: true,
                    message: 'Please input the function code!',
                  },
                ]}
              >
                <CodeMirror
                  height="500px"
                  extensions={editorExtensions}
                  onChange={() => setModified(checkCodeIsModified())}
                />
              </Form.Item>
          }
        </Space>

        <Form.Item>
          <Space>
            <Button type="primary" htmlType="submit" disabled={!isModified}>
              Update
            </Button>
            {!func.is_file &&
            <Button type="default" onClick={() => resetFields()} disabled={!isModified}>
              Reset
            </Button> }
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
