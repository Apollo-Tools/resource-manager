import {Button, Form, Input} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/FormValidationRules';
import {createServiceType} from '../../lib/ServiceTypeService';
import {createFunctionType} from '../../lib/FunctionTypeService';

const NewFunctionFrom = ({setNewArtifactType, artifact}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      if (artifact === 'service') {
        await createServiceType(values.name, token, setNewArtifactType, setError);
      } else {
        await createFunctionType(values.name, token, setNewArtifactType, setError);
      }
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <>
      <Form
        name="newArtifactTypeForm"
        form={form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
          <Form.Item
            label="Name"
            name="name"
            rules={[nameValidationRule, nameRegexValidationRule]}
            className="col-span-6"
          >
            <Input className="w-40" />
          </Form.Item>
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
  artifact: PropTypes.oneOf(['service', 'function']).isRequired,
  setNewArtifactType: PropTypes.func.isRequired,
};

export default NewFunctionFrom;
