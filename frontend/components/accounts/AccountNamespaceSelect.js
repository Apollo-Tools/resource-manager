import {Select, Space} from 'antd';
import {useState} from 'react';
import PropTypes from 'prop-types';


const AccountNamespaceSelect = ({value = {}, onChange, namespaces}) => {
  const [selectedNamespace, setSelectedNamespace] = useState();
  const [selectedResource, setSelectedResource] = useState();
  const triggerChange = (changedValue) => {
    setSelectedNamespace(changedValue);
    onChange?.(changedValue);
  };

  return (
    <Space>
      <Select className="w-40" onChange={(resource) => {
        setSelectedResource(resource);
        triggerChange(null);
      }}
      placeholder="Select a resource"
      >
        {Array.from(namespaces.keys())
            .map((resource) => {
              const resourceData = JSON.parse(resource);
              return (
                <Select.Option value={resource} key={resource} >
                  {resourceData.name}
                </Select.Option>
              );
            })}
      </Select>
      {selectedResource &&
        <Select className="w-40" onChange={(selectedNamespace) => triggerChange(selectedNamespace)}
          value={selectedNamespace}
          placeholder="Select a namespace"
        >
          {namespaces.get(selectedResource)
              .map((namespace) => {
                return (
                  <Select.Option value={namespace.namespace_id} key={namespace.namespace_id} >
                    {namespace.namespace}
                  </Select.Option>
                );
              })}
        </Select>
      }
    </Space>
  );
};

AccountNamespaceSelect.propTypes = {
  value: PropTypes.number,
  onChange: PropTypes.func,
  namespaces: PropTypes.instanceOf(Map).isRequired,
};

export default AccountNamespaceSelect;
