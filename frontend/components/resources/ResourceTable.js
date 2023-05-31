import DateFormatter from '../misc/DateFormatter';
import {Button, Space, Table, Tooltip} from 'antd';
import Link from 'next/link';
import {DeleteOutlined, InfoCircleOutlined} from '@ant-design/icons';
import ProviderIcon from '../misc/ProviderIcon';
import PropTypes from 'prop-types';

const {Column} = Table;

const ResourceTable = ({
  resources,
  onDelete,
  hasActions,
  rowSelection,
  customButton,
  getRowClassname,
}) => {
  return (
    <Table dataSource={resources}
      rowKey={(record) => record.resource_id}
      rowSelection={rowSelection}
      size={'small'}
      rowClassName={(record) => {
        if (getRowClassname) {
          return getRowClassname(record);
        }
        return '';
      }}
    >
      <Column title="Id" dataIndex="resource_id" key="id"
        sorter={(a, b) => a.resource_id - b.resource_id}
        defaultSortOrder="ascend"
      />
      <Column title="Type" dataIndex="resource_type" key="resource_type"
        render={(resourceType) => resourceType.resource_type }
        sorter={(a, b) =>
          a.resource_type.resource_type.localeCompare(b.resource_type.resource_type)}
      />
      <Column title="Region" dataIndex="region" key="region"
        render={(region) => <>
          <ProviderIcon provider={region.resource_provider.provider} className="mr-2"/>
          {region.name}
        </>}
        sorter={(a, b) =>
          a.region.resource_provider.provider.localeCompare(b.region.resource_provider.provider) ||
          a.region.name.localeCompare(b.region.name)}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      {hasActions && (<Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={`/resources/${record.resource_id}`}>
                <Button icon={<InfoCircleOutlined />}/>
              </Link>
            </Tooltip>
            {
              customButton &&
              (<Tooltip title={customButton?.tooltip}>
                <Button onClick={() => customButton.onClick(record.resource_id)} icon={customButton.icon}/>
              </Tooltip>)
            }
            {onDelete && (
              <Tooltip title="Delete">
                <Button onClick={() => onDelete(record.resource_id)} icon={<DeleteOutlined />}/>
              </Tooltip>)}
          </Space>
        )}
      />)}
    </Table>
  );
};

ResourceTable.propTypes={
  resources: PropTypes.array.isRequired,
  onDelete: PropTypes.func,
  hasActions: PropTypes.bool,
  rowSelection: PropTypes.object,
  customButton: PropTypes.shape({onClick: PropTypes.func, icon: PropTypes.node, tooltip: PropTypes.node}),
  getRowClassname: PropTypes.func,
};


export default ResourceTable;
