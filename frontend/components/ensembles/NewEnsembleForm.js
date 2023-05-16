import {Button, Form, Input, message} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import SLOSelection from '../misc/SLOSelection';
import {listResourcesBySLOs} from '../../lib/ResourceService';
import {SearchOutlined} from '@ant-design/icons';
import ResourceTableFormItem from '../resources/ResourceTableFormItem';
import {createEnsemble} from '../../lib/EnsembleService';

const NewEnsembleForm = ({setNewEnsemble}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [resources, setResources] = useState([]);
  const [error, setError] = useState();
  const [messageApi, contextHolder] = message.useMessage();

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      const slos = values.slos.map((slo) => {
        return {
          name: slo.name,
          expression: slo.expression,
          value: slo.value,
        };
      });
      await createEnsemble(values.name, slos, values.resources, token, setNewEnsemble, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onClickFindResourcesBySLOs = () => {
    const slos = form.getFieldValue('slos');
    console.log(slos);
    if (slos != null && slos.length > 0) {
      const mapped = slos
          .map((slo) => {
            return {
              name: slo.name,
              expression: slo.expression,
              value: slo.value,
            };
          });
      console.log(mapped);
      form.resetFields(['resources']);
      listResourcesBySLOs(mapped, token, setResources, setError);
    } else {
      messageApi.open({
        type: 'error',
        content: 'Service level objective input not complete!',
      });
    }
  };

  return (
    <>
      {contextHolder}
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
                if (value.length < 1) {
                  return Promise.reject(new Error('At least one SLO is required'));
                }
                if (value.filter((slo) => slo.name === '' || slo.value.length < 1 || slo.expression === '')
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
        <Button type="default" icon={<SearchOutlined />} onClick={onClickFindResourcesBySLOs} className="mb-2">
          Find resources
        </Button>
        <Form.Item
          label="Resources"
          name="resources"
          validateStatus="success"
          validateTrigger=''
          rules={[
            {
              required: true,
              message: 'Please add resources!',
            },
          ]}
        >
          <ResourceTableFormItem resources={resources} />
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
