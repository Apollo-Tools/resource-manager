import {
  InfoCircleOutlined,
} from '@ant-design/icons';
import {Button, Table, Space, Tooltip} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {listMyDeployments} from '../../lib/api/DeploymentService';
import Link from 'next/link';
import DeploymentStatusBadge from './DeploymentStatusBadge';
import DateColumnRender from '../misc/DateColumnRender';
import PropTypes from 'prop-types';

const {Column} = Table;

const DeploymentTable = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [deployments, setDeployments] = useState([]);
  const [statusFilter, setStatusFilter] = useState([]);
  const [isLoading, setLoading] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listMyDeployments(token, setDeployments, setLoading, setError);
    }
  }, []);

  useEffect(() => {
    setStatusFilter(() =>
      [...new Set(deployments.map((deployment) => deployment.status_value))]
          .map((item) => {
            return {text: item, value: item};
          }));
  }, [deployments]);

  const checkFinishedAtNotNull = (deployment) => {
    return Object.hasOwn(deployment, 'finished_at') && deployment.finishedAt == null;
  };

  return (
    <Table dataSource={ deployments } rowKey={ (record) => record.deployment_id } size="small">
      <Column title="Id" dataIndex="deployment_id" key="id"
        sorter={ (a, b) => a.deployment_id - b.deployment_id }
      />
      <Column title="Status" dataIndex="status_value" key="status_value"
        render={ (statusValue) => <DeploymentStatusBadge status={statusValue}>{statusValue}</DeploymentStatusBadge> }
        sorter={(a, b) =>
          a.status_value.localeCompare(b.status_value)}
        filters={statusFilter}
        onFilter={(value, record) => record.status_value.indexOf(value) === 0}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={ (createdAt) => <DateColumnRender value={ createdAt }/> }
        sorter={ (a, b) => a.created_at - b.created_at }
        defaultSortOrder='descend'
      />
      <Column title="Finished at" dataIndex="finished_at" key="finished_at"
        render={(finishedAt, record) => checkFinishedAtNotNull(record) ?
          <DateColumnRender value={ finishedAt }/> : 'not finished'}
      />
      <Column title="Action at" key="action"
        render={ (_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={ `/deployments/${ record.deployment_id }` }>
                <Button icon={ <InfoCircleOutlined/> }/>
              </Link>
            </Tooltip>
          </Space>
        ) }
      />
    </Table>
  );
};

DeploymentTable.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default DeploymentTable;
