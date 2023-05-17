import ResourceTable from './ResourceTable';
import {useEffect, useState} from 'react';


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

  return <ResourceTable resources={resources} rowSelection={rowSelection}/>;
};

export default ResourceTableFormItem;