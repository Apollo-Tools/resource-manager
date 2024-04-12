import {Button, Table, Tooltip} from 'antd';
import {CopyOutlined, ExpandAltOutlined, ControlOutlined} from '@ant-design/icons';
import Link from 'next/link';
import PropTypes from 'prop-types';
import DeploymentStatusBadge from './DeploymentStatusBadge';
import env from '@beam-australia/react-env';
import DateColumnRender from '../misc/DateColumnRender';
import {useEffect, useState} from 'react';

const {Column} = Table;

const ResourceDeploymentTable = ({resourceDeployments, type}) => {
  const [functionDeployments, setFunctionDeployments] = useState(resourceDeployments);

  useEffect(() => {
    setFunctionDeployments(() =>
      resourceDeployments.map((functionDeployment) => ({...functionDeployment, showDirectUrl: false})),
    );
  }, [resourceDeployments]);

  const switchUrl = (functionDeploymentId) => {
    setFunctionDeployments(() => functionDeployments.map((functionDeployment) => {
      if (functionDeployment.resource_deployment_id === functionDeploymentId) {
        return {...functionDeployment, showDirectUrl: !functionDeployment.showDirectUrl};
      }
      return functionDeployment;
    }));
  };

  return (
    <Table
      dataSource={ (type === 'function' ? functionDeployments : resourceDeployments) }
      rowKey={ (record) => record.resource_deployment_id }
      size="small"
    >
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
      <Column title="Trigger url" dataIndex="rm_trigger_url" key="rm_trigger_url"
        render={(triggerUrl, record) => {
          if (triggerUrl!=='') {
            const url = record.showDirectUrl ? record.direct_trigger_url : env('API_URL') + triggerUrl;
            return (
              <span>
                {type === 'function' ?
                  <Tooltip title={`Show ${record.showDirectUrl ? 'rm' : 'direct'} trigger url`}>
                    <Button
                      className="text-gray-400 ml-1.5"
                      type="ghost"
                      onClick={() => switchUrl(record.resource_deployment_id)}
                      icon={record.showDirectUrl ? <ExpandAltOutlined /> : <ControlOutlined /> }/>
                  </Tooltip> :
                  <Button
                    className="text-gray-400 ml-1.5 cursor-default"
                    type="ghost"
                    disabled={true}
                    icon={<ControlOutlined /> }/>
                }
                {url}
                <Button className="text-gray-400 ml-1.5" type="ghost" icon={<CopyOutlined />}
                  onClick={async () => {
                    await navigator.clipboard.writeText(url);
                  }}/>
              </span>
            );
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
