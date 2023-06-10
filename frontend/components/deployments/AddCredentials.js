import {useEffect, useState} from 'react';
import {Button, Form, Input} from 'antd';
import {LockOutlined, UserOutlined} from '@ant-design/icons';
import {deployResources} from '../../lib/DeploymentService';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import TooltipIcon from '../misc/TooltipIcon';
import Link from 'next/link';


const AddCredentials = ({functionResources, serviceResources, next, prev, onSubmit}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [needsDockerCreds, setNeedsDockerCreds] = useState(false);
  const [needsK8SCreds, setNeedsK8SCreds] = useState(false);
  const [newDeployment, setNewDeployment] = useState();

  useEffect(() => {
    atLeastOneEdgeOrVMPresent();
    atLeastOneContainer();
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (newDeployment != null) {
      onSubmit?.(newDeployment);
      next();
    }
  }, [newDeployment]);

  const atLeastOneEdgeOrVMPresent = () => {
    for (const entry of functionResources.entries()) {
      const openfaasResources = entry[1].filter((resource) => ['openfaas']
          .includes(resource.platform.platform));
      if (openfaasResources.length > 0) {
        setNeedsDockerCreds(true);
        break;
      }
    }
  };

  const atLeastOneContainer = () => {
    setNeedsK8SCreds(serviceResources.size > 0);
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
    requestBody.docker_credentials = {
      username: needsDockerCreds ? values.dockerUsername : '',
      access_token: needsDockerCreds ? values.dockerAccessToken : '',
    };
    requestBody.kube_config = values.kubeconfig;
    requestBody.function_resources = functionDeployments;
    requestBody.service_resources = serviceDeployments;
    if (!checkTokenExpired()) {
      deployResources(requestBody, token, setNewDeployment, setError);
    }
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
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

        <Form.Item
          label={<>
            Kube Config
            <TooltipIcon text={<>see section
              <Link legacyBehavior={true} href="https://github.com/Apollo-Tools/resource-manager/blob/main/backend/src/main/resources/openapi/resource-manager.yaml">
                Deployments
              </Link> for an example
            </>} />
          </>}
          name="kubeconfig"
          hidden={!needsK8SCreds}
          rules={[
            {
              required: needsK8SCreds,
              message: 'Please input a valid kube config!',
            },
          ]}
        >
          <Input.TextArea className="h-[400px]" style={{resize: 'none'}}/>
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
  next: PropTypes.func.isRequired,
  prev: PropTypes.func.isRequired,
  onSubmit: PropTypes.func,
};

export default AddCredentials;
