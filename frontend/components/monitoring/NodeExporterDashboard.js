import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const InvocationDashboard = ({resourceIds, deploymentId, isActive = true}) => {
  const [parameters, setParameters] = useState([]);
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    const newParameters = [];
    if (resourceIds != null) {
      resourceIds.forEach((resourceId) => {
        newParameters.push({'key': 'var-resourceId', 'value': resourceId.toString()});
      });
      newParameters.push({'key': 'var-deploymentId', 'value': deploymentId});
      newParameters.push({'key': 'var-interval', 'value': '15s'});
      newParameters.push({'key': 'refresh', 'value': '5s'});
      setParameters(() => newParameters);
      setParamsInitialized(true);
    }
  }, [resourceIds]);

  if (!paramsInitialized || !isActive) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='efa71f23-5850-4144-8b29-0ae9480eec94/node-exporter'
      parameters={parameters}
      className="w-full h-[450px]"
    />
  );
};

InvocationDashboard.propTypes = {
  resourceIds: PropTypes.instanceOf(Set).isRequired,
  deploymentId: PropTypes.number.isRequired,
  isActive: PropTypes.bool,
};

export default InvocationDashboard;
