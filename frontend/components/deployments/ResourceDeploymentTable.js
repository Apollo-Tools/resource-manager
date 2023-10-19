import {Button, Table} from 'antd';
import {CopyOutlined} from '@ant-design/icons';
import Link from 'next/link';
import PropTypes from 'prop-types';
import DeploymentStatusBadge from './DeploymentStatusBadge';
import env from '@beam-australia/react-env';
import DateColumnRender from '../misc/DateColumnRender';

const {Column} = Table;

const ResourceDeploymentTable = ({resourceDeployments, type}) => {
  return (
    <Table dataSource={ resourceDeployments } rowKey={ (record) => record.resource_deployment_id } size="small">
      <Column title="Id" dataIndex="resource_deployment_id" key="resource_deployment_id"
        sorter={ (a, b) => a.resource_deployment_id - b.resource_deployment_id }
      />
      {type === 'function' &&<Column title='Function' dataIndex={['function']} key="function"
        render={(func) =>
          <Link href={`/functions/${func.function_id}`}>
            <Button type="link" size="small">{func.name}</Button>
          </Link>}
        sorter={ (a, b) => a.function.name.localeCompare(b.function.name) }
      /> }
      {type==='service' && <Column title='Service' dataIndex={['service']} key="function"
        render={(service) =>
          <Link href={`/services/${service.service_id}`}>
            <Button type="link" size="small">{service.name}</Button>
          </Link>}
        sorter={ (a, b) => a.service.name.localeCompare(b.service.name) }
      />}

      <Column title="Resource" dataIndex={['resource']} key="resource"
        render={(resource) =>
          <Link href={`/resources/${resource.resource_id}`}>
            <Button type="link" size="small">{resource.resource_id}</Button>
          </Link>}
        sorter={ (a, b) => a.resource.resource_id - b.resource.resource_id }
      />
      <Column title="Trigger url" dataIndex="trigger_url" key="trigger_url"
        sorter={ (a, b) => a.trigger_url.localeCompare(b.trigger_url) }
        render={(triggerUrl) => {
          if (triggerUrl!=='') {
            const url = (type==='service' ? env('API_URL') : '') + triggerUrl;
            return <span>{url} <Button className="text-gray-400 ml-1.5" type="ghost" icon={<CopyOutlined />}
              onClick={async () => {
                await navigator.clipboard.writeText(url);
              }}/></span>;
          } else {
            return <>Not available ...</>;
          }
        }
        }
      />
      <Column title="Status" dataIndex="status" key="status"
        render={(status) =>
          <DeploymentStatusBadge status={status.status_value}>
            {status.status_value}
          </DeploymentStatusBadge>
        }
        sorter={ (a, b) => a.status_value - b.status_value }
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={ (createdAt) => <DateColumnRender value={createdAt}/> }
        sorter={ (a, b) => a.created_at - b.created_at }
      />
    </Table>);
};

ResourceDeploymentTable.propTypes = {
  resourceDeployments: PropTypes.array.isRequired,
  type: PropTypes.oneOf(['function', 'service']),
};

export default ResourceDeploymentTable;
