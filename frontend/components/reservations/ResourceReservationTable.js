import { Button, Table } from 'antd';
import { CheckCircleTwoTone, CloseCircleTwoTone } from '@ant-design/icons';
import DateFormatter from '../misc/DateFormatter';
import Link from 'next/link';

const Column = Table;

const ResourceReservationTable = ({resourceReservations}) => {
  return (
    <Table dataSource={ resourceReservations } rowKey={ (record) => record.resource_reservation_id }>
      <Column title="Id" dataIndex="resource_reservation_id" key="resource_reservation_id"
              sorter={ (a, b) => a.resource_reservation_id - b.resource_reservation_id }
      />
      <Column title="Function" dataIndex={["function_resource", "function"]} key="function"
              render={(func) =>
                <Link href={`/functions/${func.function_id}`}>
                  <Button type="link" size="small">{func.name}</Button>
                </Link>}
              sorter={ (a, b) => a.function_resource.function.name.localeCompare(b.function_resource.function.name) }
      />
      <Column title="Resource" dataIndex={["function_resource", "resource"]} key="resource"
              render={(resource) =>
                <Link href={`/resources/${resource.resource_id}`}>
                  <Button type="link" size="small">{resource.resource_id}</Button>
                </Link>}
              sorter={ (a, b) => a.function_resource.resource.resource_id - b.function_resource.resource.resource_id }
      />
      <Column title="Trigger url" dataIndex="trigger_url" key="trigger_url"
              sorter={ (a, b) => a.trigger_url.localeCompare(b.trigger_url) }
      />
      <Column title="Is deployed" dataIndex="is_deployed" key="is_deployed"
              render={ (isDeployed) => isDeployed ? <CheckCircleTwoTone twoToneColor="#00ff00" className="text-lg"/> :
                <CloseCircleTwoTone twoToneColor="#ff0000" className="text-lg"/> }
              sorter={ (a, b) => {
                if (a.is_deployed && b.is_deployed) return 0;
                else if (a.is_deployed) return -1;
                else if (b.is_deployed) return 1;
              }}
              defaultSortOrder="ascend"
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
              render={ (createdAt) => <DateFormatter dateTimestamp={ createdAt }/> }
              sorter={ (a, b) => a.created_at - b.created_at }
      />
    </Table>);
}

export default ResourceReservationTable;