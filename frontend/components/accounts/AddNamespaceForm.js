import {Button, Form} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {listK8sNamespaces} from '../../lib/api/K8sNamespaceService';
import AccountNamespaceSelect from './AccountNamespaceSelect';
import {addAccountNamespace} from '../../lib/api/AccountNamespaceService';
import PropTypes from 'prop-types';


const AddNamespaceForm = ({accountId, existingNamespaces, setFinished, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState();
  const [namespaces, setNamespaces] = useState();
  const [filteredNamespaces, setFilteredNamespaces] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listK8sNamespaces(token, setNamespaces, setLoading, setError);
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

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      addAccountNamespace(accountId, values.namespace, token, setLoading, setError)
          .then(() => setFinished(true))
          .then(() => form.resetFields());
    }
  };

  if (!filteredNamespaces || filteredNamespaces.size === 0) {
    return;
  }

  return <Form
    form={form}
    name="addNamespaceForm"
    onFinish={onFinish}
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
        <AccountNamespaceSelect namespaces={filteredNamespaces} />
      </Form.Item>
    </div>
    <Form.Item>
      <Button type="primary" htmlType="submit" loading={isLoading}>
        Add
      </Button>
    </Form.Item>
  </Form>;
};

AddNamespaceForm.propTypes = {
  accountId: PropTypes.number.isRequired,
  existingNamespaces: PropTypes.arrayOf(PropTypes.object).isRequired,
  setFinished: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default AddNamespaceForm;
