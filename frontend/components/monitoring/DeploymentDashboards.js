import InvocationDashboard from './InvocationDashboard';
import NodeExporterDashboard from './NodeExporterDashboard';
import PropTypes from 'prop-types';
import {Collapse} from 'antd';
const {Panel} = Collapse;


const DeploymentDashboards = ({deploymentId, functionResourceIds, isActive = true}) => {
  return (
    <>
      <Collapse accordion size="small" bordered={false} >
        {functionResourceIds != null && functionResourceIds.length !== 0 &&
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
      </Collapse>
    </>
  );
};

DeploymentDashboards.propTypes = {
  deploymentId: PropTypes.number.isRequired,
  functionResourceIds: PropTypes.arrayOf(PropTypes.number).isRequired,
  isActive: PropTypes.bool,
};

export default DeploymentDashboards;
