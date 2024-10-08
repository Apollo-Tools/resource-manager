import {Button, Form, Upload} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useState} from 'react';
import PropTypes from 'prop-types';
import {updateFunctionUpload} from '../../lib/api/FunctionService';
import {PlusOutlined} from '@ant-design/icons';

const UpdateFunctionFileForm = ({func, reloadFunction, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isModified, setModified] = useState(false);
  const [fileList, setFileList] = useState([]);
  const [isLoading, setLoading] = useState(false);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await updateFunctionUpload(func.function_id, values.upload.originFileObj, token, setLoading, setError)
          .then(() => reloadFunction().then(() => setModified(false)))
          .then(() => setFileList([]));
    }
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
        autoComplete="off"
        layout="vertical"
      >
        {
          func?.is_file &&
            <>
              <Form.Item
                label="Update code (.zip)"
                name="upload"
                rules={[
                  {
                    required: true,
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
                className="lg:col-span-12 col-span-6"
                valuePropName='file'
              >
                <Upload
                  accept=".zip"
                  maxCount={1}
                  multiple={false}
                  listType="picture-card"
                  showUploadList={{showPreviewIcon: false}}
                  onChange={({file, fileList}) => setModified(checkFileIsModified(file, fileList))}
                  fileList={fileList}
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
              </Form.Item>
              <Form.Item>
                <Button type="primary" htmlType="submit" disabled={!isModified} loading={isLoading}>
                  Update
                </Button>
              </Form.Item>
            </>
        }
      </Form>
    </>
  );
};

UpdateFunctionFileForm.propTypes = {
  func: PropTypes.object.isRequired,
  reloadFunction: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default UpdateFunctionFileForm;
