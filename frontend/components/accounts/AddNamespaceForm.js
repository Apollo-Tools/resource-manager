import {Button, Form} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {listK8sNamespaces} from '../../lib/K8sNamespaceService';
import AccountNamespaceSelect from './AccountNamespaceSelect';
import {addAccountNamespace} from '../../lib/AccountNamespaceService';
import PropTypes from 'prop-types';


const AddNamespaceForm = ({accountId, existingNamespaces, setFinished}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [namespaces, setNamespaces] = useState();
  const [filteredNamespaces, setFilteredNamespaces] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listK8sNamespaces(token, setNamespaces, setError);
    }
  }, []);

  useEffect(() => {
    if (namespaces != null && existingNamespaces != null) {
      const namespaceResources = [...new Set(existingNamespaces.map((namespace) => namespace.resource.resource_id))];
      const namespaceMap = new Map();
      namespaces.filter((namespace) => !(namespaceResources.includes(namespace.resource.resource_id)))
          .forEach((namespace) => {
            const resource = JSON.stringify(namespace.resource);
            if (!namespaceMap.has(resource)) {
              namespaceMap.set(resource, []);
            }
            namespaceMap.get(resource).push(namespace);
          });
      setFilteredNamespaces(() => namespaceMap);
    }
  }, [namespaces, existingNamespaces]);

  useEffect(() => {
    console.log(filteredNamespaces);
  }, [filteredNamespaces]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      addAccountNamespace(accountId, values.namespace, token, setError)
          .then(() => setFinished(true))
          .then(() => form.resetFields());
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  if (!filteredNamespaces || filteredNamespaces.size === 0) {
    return;
  }

  return <Form
    form={form}
    name="addNamespaceForm"
    onFinish={onFinish}
    onFinishFailed={onFinishFailed}
    autoComplete="off"
    layout="vertical"
  >
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <Form.Item
        label="Namespace"
        name="namespace"
        rules={[
          {
            required: true,
            message: 'Please select a namespace!',
          },
        ]}
        className="col-span-6"
      >
        <AccountNamespaceSelect namespaces={filteredNamespaces} onChange={(value) => console.log(value)} />
      </Form.Item>
    </div>
    <Form.Item>
      <Button type="primary" htmlType="submit">
        Add
      </Button>
    </Form.Item>
  </Form>;
};

AddNamespaceForm.propTypes = {
  accountId: PropTypes.number.isRequired,
  existingNamespaces: PropTypes.arrayOf(PropTypes.object).isRequired,
  setFinished: PropTypes.bool.isRequired,
};

export default AddNamespaceForm;
