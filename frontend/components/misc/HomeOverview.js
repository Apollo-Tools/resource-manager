import {Typography} from 'antd';
import RegionDashboard from '../monitoring/RegionDashboard';
import K8sResourceDashboard from '../monitoring/K8sResourceDashboard';
import NodeExporterDashboard from '../monitoring/NodeExporterDashboard';

const {Title} = Typography;

const HomeOverview = () => {
  return (
    <>
      <Title level={2}>Overview</Title>
      <RegionDashboard />
      <NodeExporterDashboard deploymentId={-1} resourceIds={new Set(['All'])} />
      <K8sResourceDashboard resourceIds={new Set(['All'])} />
    </>
  );
};

export default HomeOverview;
