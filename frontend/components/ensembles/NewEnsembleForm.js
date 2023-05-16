import {Button, Form, Input} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import SLOSelection from '../misc/SLOSelection';

const NewEnsembleForm = ({setNewEnsemble}) => {
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
      // await createFunction(values.runtime, values.name, values.code, token, setNewEnsemble, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
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
          label="Service Level Objectives"
          name="slos"
          valuePropName="slos"
          validateStatus="success"
          validateTrigger=''
          rules={[
            {
              required: true,
              message: 'Please add slos!',
            },
            () => ({
              validator(_, value) {
                console.log(value);
                const slos = value.slos;
                if (slos.length < 1) {
                  return Promise.reject(new Error('At least one SLO is required'));
                }
                if (slos.filter((slo) => slo.name === '' || slo.value.length < 1 || slo.expression === '')
                    .length > 0) {
                  return Promise.reject(new Error('SLOs are not complete'));
                }

                return Promise.resolve();
              },
            }),
          ]}
        >
          <SLOSelection />

        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewEnsembleForm.propTypes = {
  setNewEnsemble: PropTypes.func.isRequired,
};

export default NewEnsembleForm;
