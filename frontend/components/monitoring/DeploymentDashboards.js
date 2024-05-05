import InvocationDashboard from './InvocationDashboard';
import NodeExporterDashboard from './NodeExporterDashboard';
import PropTypes from 'prop-types';
import {Collapse} from 'antd';
import K8sResourceDashboard from './K8sResourceDashboard';
import K8sPodDashboard from './K8sPodDashboard';
import {useEffect, useState} from 'react';
import ServiceDashboard from './ServiceDashboard';


const DeploymentDashboards = ({deploymentId, functionResourceIds, serviceResourceIds, isActive = true}) => {
  const [collapseItems, setCollapseItems] = useState([]);

  useEffect(() => {
    const newCollapseItems = [];
    if (functionResourceIds != null && functionResourceIds.size !== 0) {
      newCollapseItems.push({
        key: 0,
        label: 'Function Invocations',
        children: (
          <div className="m-[-12px] mb-[-18px] mt-[0px]">
            <InvocationDashboard deploymentId={deploymentId} isActive={isActive}/>
          </div>
        ),
      });
      if (isActive) {
        newCollapseItems.push({
          key: 1,
          label: 'VM/Bare-Metal Devices',
          children: (
            <div className="m-[-12px] mb-[-18px] mt-[0px]">
              <NodeExporterDashboard
                resourceIds={functionResourceIds}
                isActive={isActive}
                deploymentId={deploymentId}
              />
            </div>
          ),
        });
      }
    }
    if (serviceResourceIds != null && serviceResourceIds.size !== 0 && isActive) {
      newCollapseItems.push({
        key: 2,
        label: 'K8s Resources',
        children: (
          <>
            <div className="m-[-12px] mb-[-18px] mt-[0px]">
              <ServiceDashboard
                deploymentId={deploymentId}
                isActive={isActive}
              />
            </div>
            <div className="m-[-12px] mb-[-18px] mt-[0px]">
              <K8sPodDashboard
                deploymentId={deploymentId}
                isActive={isActive}
              />
            </div>
            <div className="m-[-12px] mb-[-18px] mt-[0px]">
              <K8sResourceDashboard
                resourceIds={serviceResourceIds}
                isActive={isActive}
              />
            </div>
          </>
        ),
      })
      ;
    }
    setCollapseItems(newCollapseItems);
  }, [functionResourceIds, serviceResourceIds, isActive]);


  return (
    <>
      <Collapse accordion size="small" bordered={false} items={collapseItems}/>
    </>
  );
};

DeploymentDashboards.propTypes = {
  deploymentId: PropTypes.number.isRequired,
  functionResourceIds: PropTypes.instanceOf(Set).isRequired,
  serviceResourceIds: PropTypes.instanceOf(Set).isRequired,
  isActive: PropTypes.bool,
};

export default DeploymentDashboards;
