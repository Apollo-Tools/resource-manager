import InvocationDashboard from './InvocationDashboard';
import NodeExporterDashboard from './NodeExporterDashboard';
import PropTypes from 'prop-types';
import {Collapse} from 'antd';
import K8sResourceDashboard from './K8sResourceDashboard';
const {Panel} = Collapse;


const DeploymentDashboards = ({deploymentId, functionResourceIds, serviceResourceIds, isActive = true}) => {
  return (
    <>
      <Collapse accordion size="small" bordered={false} >
        {functionResourceIds != null && functionResourceIds.size !== 0 &&
          (<>
            <Panel header="Function Invocations" key={0}>
              <div className="m-[-12px] mb-[-18px] mt-[0px]">
                <InvocationDashboard deploymentId={deploymentId} isActive={isActive} />
              </div>
            </Panel>
            {isActive && <Panel header="VM/Bare-Metal Devices" key={1} >
              <div className="m-[-12px] mb-[-18px] mt-[0px]">
                <NodeExporterDashboard
                  resourceIds={functionResourceIds}
                  isActive={isActive}
                  deploymentId={deploymentId}
                />
              </div>
            </Panel>}
          </>)
        }
        {serviceResourceIds != null && serviceResourceIds.size !== 0 && isActive &&
          (<Panel header="K8s Resources" key={2} >
            <div className="m-[-12px] mb-[-18px] mt-[0px]">
              <K8sResourceDashboard
                resourceIds={serviceResourceIds}
                isActive={isActive}
              />
            </div>
          </Panel>)
        }
      </Collapse>
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
