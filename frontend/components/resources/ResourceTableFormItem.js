import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import ResourceTable from './ResourceTable';


const ResourceTableFormItem = ({onChange, resources}) => {
  const [selected, setSelected] = useState();

  useEffect(() => {
    triggerChange(selected);
  }, [selected]);

  const rowSelection = {
    onChange: (selectedRowKeys, selectedRows) => {
      setSelected(() => selectedRows.map((row) => {
        return {resource_id: row.resource_id};
      }));
    },
  };

  const triggerChange = (changedValue) => {
    onChange?.(changedValue);
  };

  return <ResourceTable resources={resources} rowSelection={rowSelection} hasActions resourceType='ensemble' />;
};

ResourceTableFormItem.propTypes = {
  onChange: PropTypes.func,
  resources: PropTypes.arrayOf(PropTypes.object),
};

export default ResourceTableFormItem;
