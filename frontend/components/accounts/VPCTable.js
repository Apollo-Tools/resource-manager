import {Button, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import ProviderIcon from '../misc/ProviderIcon';
import PropTypes from 'prop-types';
import DateColumnRender from "../misc/DateColumnRender";

const {Column} = Table;
const {confirm} = Modal;

const VPCTable = ({
  vpcs,
  onDelete,
  hasActions,
}) => {
  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this virtual private cloud?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onDelete(id);
      },
    });
  };

  return (
    <Table dataSource={vpcs} rowKey={(record) => record.vpc_id} size={'small'}>
      <Column title="Id" dataIndex="vpc_id" key="id"
        sorter={(a, b) => a.vpc_id - b.vpc_id}
        defaultSortOrder="ascend"
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
      <Column title="VPC Id" dataIndex="vpc_id_value" key="vpc_id_value"
        sorter={(a, b) =>
          a.vpc_id_value.localeCompare(b.vpc_id_value)}
      />
      <Column title="Subnet Id" dataIndex="subnet_id_value" key="subnet_id_value"
        sorter={(a, b) =>
          a.subnet_id_value.localeCompare(b.subnet_id_value)}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      {hasActions && (<Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            {onDelete && (<Button onClick={() => showDeleteConfirm(record.vpc_id)} icon={<DeleteOutlined />}/>)}
          </Space>
        )}
      />)}
    </Table>
  );
};

VPCTable.propTypes={
  vpcs: PropTypes.array.isRequired,
  onDelete: PropTypes.func,
  hasActions: PropTypes.bool,
};

export default VPCTable;
