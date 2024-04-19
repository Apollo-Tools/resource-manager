import {useEffect, useState} from 'react';
import {Button, Form, Input, Modal} from 'antd';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {deployResources} from '../../lib/api/DeploymentService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import NothingToSelectCard from './NothingToSelectCard';
import LoadingSpinner from '../misc/LoadingSpinner';


const AddCredentials = ({functionResources, serviceResources, lockResources, ensembleId, alertingUrl, next, prev,
  onSubmit, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);
  const [needsDockerCreds, setNeedsDockerCreds] = useState(false);
  const [newDeployment, setNewDeployment] = useState();

  useEffect(() => {
    atLeastOneEdgeOrVMPresent();
  }, []);

  useEffect(() => {
    if (newDeployment != null) {
      onSubmit?.(newDeployment);
      next();
    }
  }, [newDeployment]);

  const atLeastOneEdgeOrVMPresent = () => {
    for (const entry of functionResources.entries()) {
      const openfaasResources = entry[1].filter((resource) => ['openfaas', 'ec2']
          .includes(resource.platform.platform));
      if (openfaasResources.length > 0) {
        setNeedsDockerCreds(true);
        break;
      }
    }
  };

  const onFinish = async (values) => {
    const requestBody = {};
    const functionDeployments = [];
    const serviceDeployments = [];
    functionResources.forEach((resources, functionId) => {
      resources.forEach((resource) => {
        functionDeployments.push({
          function_id: functionId,
          resource_id: resource.resource_id,
        });
      });
    });
    serviceResources.forEach((resources, serviceId) => {
      resources.forEach((resource) => {
        serviceDeployments.push({
          service_id: serviceId,
          resource_id: resource.resource_id,
        });
      });
    });
    const dockerCredentials = {
      registry: values.dockerRegistry,
      username: values.dockerUsername,
      access_token: values.dockerAccessToken,
    };
    requestBody.credentials = {
      ...(needsDockerCreds && {docker_credentials: dockerCredentials}),
    };
    requestBody.function_resources = functionDeployments;
    requestBody.service_resources = serviceDeployments;
    requestBody.lock_resources = lockResources.map((resourceId) => {
      return {resource_id: resourceId};
    });
    if (alertingUrl != null) {
      requestBody.validation = {
        ensemble_id: ensembleId,
        alert_notification_url: alertingUrl,
      };
    }
    if (!checkTokenExpired()) {
      await deployResources(requestBody, token, setNewDeployment, setLoading, setError);
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <>
      <Modal open={isLoading} footer={null} closable={false}>
        <LoadingSpinner isCard={false}/>
      </Modal>;
      <Form
        form={form}
        name="dockerCredentials"
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        {!needsDockerCreds && <NothingToSelectCard text="No credentials required for this deployment"/>}
        <Form.Item
          label="Docker registry"
          name="dockerRegistry"
          hidden={!needsDockerCreds}
          rules={[
            {
              required: needsDockerCreds,
              message: 'Please input a valid docker registry!',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="docker.io"/>
        </Form.Item>

        <Form.Item
          label="Docker Username"
          name="dockerUsername"
          hidden={!needsDockerCreds}
          rules={[
            {
              required: needsDockerCreds,
              message: 'Please input your docker username!',
            },
          ]}
        >
          <Input prefix={<UserOutlined className="site-form-item-icon" />} placeholder="Docker Username"/>
        </Form.Item>

        <Form.Item
          label="Docker Access Token"
          name="dockerAccessToken"
          hidden={!needsDockerCreds}
          rules={[
            {
              required: needsDockerCreds,
              message: 'Please input a valid docker access token with write permissions!',
            },
          ]}
        >
          <Input.Password prefix={<LockOutlined className="site-form-item-icon" />}/>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" className="float-right">Deploy</Button>
          <Button type="default" onClick={prev} className="float-left">Back</Button>
        </Form.Item>
      </Form>
    </>
  );
};

AddCredentials.propTypes = {
  functionResources: PropTypes.instanceOf(Map).isRequired,
  serviceResources: PropTypes.instanceOf(Map).isRequired,
  lockResources: PropTypes.arrayOf(PropTypes.number).isRequired,
  ensembleId: PropTypes.number,
  alertingUrl: PropTypes.string,
  next: PropTypes.func.isRequired,
  prev: PropTypes.func.isRequired,
  onSubmit: PropTypes.func,
  setError: PropTypes.func.isRequired,
};

export default AddCredentials;
