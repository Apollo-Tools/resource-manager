import {Button, Form, Input} from 'antd';
import {useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/api/FormValidationRules';
import {createServiceType} from '../../lib/api/ServiceTypeService';
import {createFunctionType} from '../../lib/api/FunctionTypeService';

const NewArtifactTypeForm = ({setNewArtifactType, artifact, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState();

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      if (artifact === 'service') {
        await createServiceType(values.name, token, setNewArtifactType, setLoading, setError);
      } else {
        await createFunctionType(values.name, token, setNewArtifactType, setLoading, setError);
      }
    }
  };

  return (
    <>
      <Form
        name="newArtifactTypeForm"
        form={form}
        onFinish={onFinish}
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
          <Button type="primary" htmlType="submit" loading={isLoading}>
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewArtifactTypeForm.propTypes = {
  artifact: PropTypes.oneOf(['service', 'function']).isRequired,
  setNewArtifactType: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default NewArtifactTypeForm;
