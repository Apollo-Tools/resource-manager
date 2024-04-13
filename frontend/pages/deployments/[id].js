import {Button, Divider, Modal, Tooltip, Typography} from 'antd';
import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {cancelDeployment, getDeployment, listDeploymentLogs} from '../../lib/api/DeploymentService';
import ResourceDeploymentTable from '../../components/deployments/ResourceDeploymentTable';
import DeploymentStatusCircle from '../../components/deployments/DeploymentStatusCircle';
import {useInterval} from '../../lib/hooks/useInterval';
import LogsDisplay from '../../components/logs/LogsDisplay';
import {DisconnectOutlined, ExclamationCircleFilled, ReloadOutlined} from '@ant-design/icons';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {listLockedResources} from '../../lib/api/ResourceService';
import ResourceTable from '../../components/resources/ResourceTable';
import DeploymentDetailsCard from '../../components/deployments/DeploymentDetailsCard';
import DeploymentDashboards from '../../components/monitoring/DeploymentDashboards';
import PropTypes from 'prop-types';

const {confirm} = Modal;

const DeploymentDetails = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);
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
  const [functionResourceIds, setFunctionResourceIds] = useState(new Set());
  const [serviceResourceIds, setServiceResourceIds] = useState(new Set());
  const [lockedResources, setLockedResources] = useState([]);
  const router = useRouter();
  const id = parseInt(router.query.id);

  useEffect(() => {
    if (!checkTokenExpired() && id !== undefined) {
      void refreshDeployment(setError);
    }
  }, [id]);

  useEffect(() => {
    if (deployment != null) {
      checkDeploymentStatus();
      updateResourceIds();
    }
  }, [deployment]);

  useEffect(() => {
    if (deployment == null || deploymentStatus.isNew || deploymentStatus.isTerminating) {
      setPollingDelay(process.env.NEXT_PUBLIC_POLLING_DELAY);
    }
  }, [deploymentStatus]);

  useInterval(async () => {
    if (!checkTokenExpired() && deployment != null) {
      await refreshDeployment(setError);
    }
  }, pollingDelay);

  const refreshDeployment = async ({setError}) => {
    setPollingDelay(null);
    await getDeployment(id, token, setDeployment, setLoading, setError);
    await listDeploymentLogs(id, token, setLogs, setLoading, setError);
    await listLockedResources(id, token, setLockedResources, setError);
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

  const updateResourceIds = () => {
    setFunctionResourceIds(() => new Set(
        deployment.function_resources?.map((functionResource) => functionResource.resource.resource_id),
    ),
    );
    setServiceResourceIds(() => new Set(
        deployment.service_resources?.map((serviceResource) => {
          if (Object.hasOwn(serviceResource.resource, 'main_resource')) {
            return serviceResource.resource.main_resource.resource_id;
          }
          return serviceResource.resource.resource_id;
        }),
    ),
    );
  };

  const existResourceDeploymentsByStatusValue = (resourceDeployments, statusValue) => {
    return resourceDeployments.filter((deployment) => {
      return deployment.status.status_value === statusValue;
    }).length !== 0;
  };

  const onClickCancel = async (id) => {
    if (!checkTokenExpired()) {
      await cancelDeployment(id, token, setLoading, setError)
          .then(() => refreshDeployment(setError));
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
      async onOk() {
        await onClickCancel(id);
      },
    });
  };

  if (deployment == null) {
    return <></>;
  }

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Deployment Details`}</title>
      </Head>
      <div className="default-card">
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
            <Button onClick={ () => showCancelConfirm(id) }
              icon={ <DisconnectOutlined/> } className="ml-2 bg-red-50 text-red-500 border-red-500"/>
          </Tooltip>
            }
          </div>
        </Typography.Title>
        <DeploymentDetailsCard deployment={deployment} />
        <Typography.Title level={3}>Resource Deployments</Typography.Title>
        <Divider/>
        {deployment.function_resources.length > 0 &&
          <ResourceDeploymentTable resourceDeployments={deployment.function_resources} type='function'/>}
        {deployment.service_resources.length > 0 &&
          <ResourceDeploymentTable resourceDeployments={deployment.service_resources} type='service'/>}
        <Divider />
        {lockedResources.length > 0 &&
          <>
            <Typography.Title level={3}>Locked Resources</Typography.Title>
            <ResourceTable resources={lockedResources} hasActions={true} resourceType='all'/>
            <Divider />
          </>
        }
        <Typography.Title level={3}>Logs</Typography.Title>
        <LogsDisplay logs={logs}/>
        <Divider />
        <Typography.Title level={3}>Monitoring</Typography.Title>
        <DeploymentDashboards
          deploymentId={id}
          isActive={deploymentStatus.isNew || deploymentStatus.isDeployed || deploymentStatus.isTerminating}
          functionResourceIds={functionResourceIds}
          serviceResourceIds={serviceResourceIds}
        />
      </div>
    </>
  );
};

DeploymentDetails.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default DeploymentDetails;
