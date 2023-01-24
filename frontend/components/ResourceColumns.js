import {Table} from 'antd';
import DateFormatter from './DateFormatter';
const {Column, ColumnGroup} = Table;


const ResourceColumns = () => {
  return (
    <ColumnGroup>
      <Column title="Id" dataIndex="resource_id" key="id"
        sorter={(a, b) => a.resource_id - b.resource_id}
        defaultSortOrder="ascend"
      />
      <Column title="Type" dataIndex="resource_type" key="resource_type"
        render={(resourceType) => resourceType.resource_type}
        sorter={(a, b) =>
          a.resource_type.resource_type.localeCompare(b.resource_type.resource_type)}
      />
      <Column title="Self managed" dataIndex="is_self_managed" key="is_self_managed"
        render={(isSelfManaged) => isSelfManaged.toString()}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
    </ColumnGroup>
  );
};

export default ResourceColumns;
