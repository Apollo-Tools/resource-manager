import {Button, Table} from 'antd';
import {CopyOutlined} from '@ant-design/icons';
import DateFormatter from '../misc/DateFormatter';
import Link from 'next/link';
import PropTypes from 'prop-types';
import ResourceReservationStatusBadge from './ResourceReservationStatusBadge';

const {Column} = Table;

const ResourceReservationTable = ({resourceReservations}) => {
  return (
    <Table dataSource={ resourceReservations } rowKey={ (record) => record.resource_reservation_id }>
      <Column title="Id" dataIndex="resource_reservation_id" key="resource_reservation_id"
        sorter={ (a, b) => a.resource_reservation_id - b.resource_reservation_id }
      />
      <Column title="Function" dataIndex={['function_resource', 'function']} key="function"
        render={(func) =>
          <Link href={`/functions/${func.function_id}`}>
            <Button type="link" size="small">{func.name}</Button>
          </Link>}
        sorter={ (a, b) => a.function_resource.function.name.localeCompare(b.function_resource.function.name) }
      />
      <Column title="Resource" dataIndex={['function_resource', 'resource']} key="resource"
        render={(resource) =>
          <Link href={`/resources/${resource.resource_id}`}>
            <Button type="link" size="small">{resource.resource_id}</Button>
          </Link>}
        sorter={ (a, b) => a.function_resource.resource.resource_id - b.function_resource.resource.resource_id }
      />
      <Column title="Trigger url" dataIndex="trigger_url" key="trigger_url"
        sorter={ (a, b) => a.trigger_url.localeCompare(b.trigger_url) }
        render={(triggerUrl) => {
          if (triggerUrl!=='') {
            return <span>{triggerUrl} <Button className="text-gray-400 ml-1.5" type="ghost" icon={<CopyOutlined />}
              onClick={async () => {
                await navigator.clipboard.writeText(triggerUrl);
              }}/></span>;
          } else {
            return <>Not available ...</>;
          }
        }
        }
      />
      <Column title="Status" dataIndex="status" key="status"
        render={(status) =>
          <ResourceReservationStatusBadge status={status.status_value}>
            {status.status_value}
          </ResourceReservationStatusBadge>
        }
        sorter={ (a, b) => a.status_value - b.status_value }
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={ (createdAt) => <DateFormatter dateTimestamp={ createdAt }/> }
        sorter={ (a, b) => a.created_at - b.created_at }
      />
    </Table>);
};

ResourceReservationTable.propTypes = {
  resourceReservations: PropTypes.array.isRequired,
};

export default ResourceReservationTable;
