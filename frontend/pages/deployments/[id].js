import {Button, Divider, Modal, Tooltip, Typography} from 'antd';
import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {cancelDeployment, getDeployment, listDeploymentLogs} from '../../lib/DeploymentService';
import ResourceDeploymentTable from '../../components/deployments/ResourceDeploymentTable';
import DeploymentStatusCircle from '../../components/deployments/DeploymentStatusCircle';
import {useInterval} from '../../lib/hooks/useInterval';
import LogsDisplay from '../../components/logs/LogsDisplay';
import {DisconnectOutlined, ExclamationCircleFilled, ReloadOutlined} from '@ant-design/icons';

const {confirm} = Modal;

const DeploymentDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [deployment, setDeployment] = useState();
  const [logs, setLogs] = useState([]);
  const [pollingDelay, setPollingDelay] = useState();
  const [deploymentStatus, setDeploymentStatus] = useState({
    isNew: false,
    isDeployed: false,
    isTerminating: false,
    isTerminated: false,
    isError: false,
  });
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id !== undefined) {
      refreshDeployment();
    }
  }, [id]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('action failed');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (deployment != null) {
      checkDeploymentStatus();
    }
  }, [deployment]);

  useEffect(() => {
    if (deployment == null || deploymentStatus.isNew || deploymentStatus.isTerminating) {
      setPollingDelay(process.env.NEXT_PUBLIC_POLLING_DELAY);
    }
  }, [deploymentStatus]);

  useInterval(async () => {
    if (!checkTokenExpired() && deployment != null) {
      await refreshDeployment();
    }
  }, pollingDelay);

  const refreshDeployment = async () => {
    setPollingDelay(null);
    await getDeployment(id, token, setDeployment, setError);
    await listDeploymentLogs(id, token, setLogs, setError);
  };

  const checkDeploymentStatus = () => {
    const resourceDeployments = [...deployment.function_resources, ...deployment.service_resources];
    setDeploymentStatus(() => {
      return {
        isNew: existResourceDeploymentsByStatusValue(resourceDeployments, 'NEW'),
        isDeployed: existResourceDeploymentsByStatusValue(resourceDeployments, 'DEPLOYED'),
        isTerminating: existResourceDeploymentsByStatusValue(resourceDeployments, 'TERMINATING'),
        isTerminated: existResourceDeploymentsByStatusValue(resourceDeployments, 'TERMINATED'),
        isError: existResourceDeploymentsByStatusValue(resourceDeployments, 'ERROR'),
      };
    });
  };

  const existResourceDeploymentsByStatusValue = (resourceDeployments, statusValue) => {
    return resourceDeployments.filter((deployment) => {
      return deployment.status.status_value === statusValue;
    }).length !== 0;
  };

  const onClickCancel = async (id) => {
    if (!checkTokenExpired()) {
      await cancelDeployment(id, token, setError)
          .then(() => refreshDeployment());
    }
  };

  const showCancelConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled/>,
      content: 'Are you sure you want to cancel this deployment?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickCancel(id);
      },
    });
  };

  if (deployment == null) {
    return <></>;
  }

  return (
    <div className="card container w-full md:w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={ 2 }>
        <DeploymentStatusCircle isNew={deploymentStatus.isNew}
          isDeployed={deploymentStatus.isDeployed}
          isTerminating={deploymentStatus.isTerminating}
          isTerminated={deploymentStatus.isTerminated}
          isError={deploymentStatus.isError}
        />
        Deployment Details ({ id })
        <div className="float-right">
          <Tooltip title="Refresh">
            <Button icon={<ReloadOutlined />}
              className="bg-yellow-50 text-yellow-500 border-yellow-500"
              onClick={refreshDeployment}
            />
          </Tooltip>
          {deploymentStatus.isDeployed &&
          <Tooltip title="Cancel Deployment">
            <Button disabled={!deployment.is_active} onClick={ () => showCancelConfirm(id) }
              icon={ <DisconnectOutlined/> } className="ml-2 bg-red-50 text-red-500 border-red-500"/>
          </Tooltip>
          }
        </div>
      </Typography.Title>
      <Divider/>
      {deployment.is_active &&
        <>
          {deployment.function_resources.length > 0 &&
            <ResourceDeploymentTable resourceDeployments={deployment.function_resources} type='function'/>}
          {deployment.service_resources.length > 0 &&
            <ResourceDeploymentTable resourceDeployments={deployment.service_resources} type='service'/>}
          <Divider />
        </>
      }
      <Typography.Title level={3}>Logs</Typography.Title>
      <LogsDisplay logs={logs}/>
      <Divider />
      {/* TODO: insert monitoring data*/}
      <Typography.Title level={3}>Monitoring</Typography.Title>
      <div>
        TODO display monitoring data
      </div>
    </div>
  );
};

export default DeploymentDetails;
