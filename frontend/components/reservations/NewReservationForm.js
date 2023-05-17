import {Button, Form, Input} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {reserveResources} from '../../lib/ReservationService';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import FunctionTable from '../functions/FunctionTable';
import {listResources} from '../../lib/ResourceService';


const NewReservationForm = ({setNewReservation}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [resources, setResources] = useState();
  const [error, setError] = useState(false);
  const [vmOrEdgeSelected, setVmOrEdgeSelected] = useState(false);
  const [functionResourceSelected, setFunctionResourceSelected] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResources(false, token, setResources, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      const requestBody = {};
      const functionResources = [];
      values.functionResources.selectedResourceIds.forEach((resources, functionId) => {
        resources.forEach((resourceId) => {
          functionResources.push({
            function_id: functionId,
            resource_id: resourceId,
          });
        });
      });
      if (vmOrEdgeSelected && Object.hasOwn(values, 'dockerUsername') && Object.hasOwn(values, 'dockerAccessToken')) {
        requestBody.docker_credentials = {
          username: values.dockerUsername,
          access_token: values.dockerAccessToken,
        };
      } else {
        requestBody.docker_credentials = {
          username: '',
          access_token: '',
        };
      }
      requestBody.function_resources = functionResources;
      await reserveResources(requestBody, token, setNewReservation, setError);
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onChangeFunctionResources = (values) => {
    const selectedFunctionResources = values.selectedResourceIds.values();
    let includingVMOrEdge = false;
    for (const selectedFunctionResource of selectedFunctionResources) {
      for (const selectedResource of selectedFunctionResource) {
        const resource = resources.filter((resource) => resource.resource_id === selectedResource)[0];
        if (resource.resource_type.resource_type === 'vm' || resource.resource_type.resource_type === 'edge') {
          includingVMOrEdge = true;
          break;
        }
      }
      if (includingVMOrEdge) {
        break;
      }
    }
    setVmOrEdgeSelected(includingVMOrEdge);
    setFunctionResourceSelected(values.selectedResourceIds.size > 0);
  };

  return (
    <>
      <Form
        form={form}
        name="dockerCredentials"
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <Form.Item name="functionResources">
          <FunctionTable hideDelete isExpandable />
        </Form.Item>
        <Form.Item
          label="Docker Username"
          name="dockerUsername"
          hidden={!vmOrEdgeSelected}
          rules={[
            {
              required: vmOrEdgeSelected,
              message: 'Please input your docker username!',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Docker Username"/>
        </Form.Item>

        <Form.Item
          label="Docker Access Token"
          name="dockerAccessToken"
          hidden={!vmOrEdgeSelected}
          rules={[
            {
              required: vmOrEdgeSelected,
              message: 'Please input a valid docker access token with write permissions!',
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" disabled={!functionResourceSelected}>
            Reserve
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewReservationForm.propTypes = {
  setNewReservation: PropTypes.func.isRequired,
};

export default NewReservationForm;
