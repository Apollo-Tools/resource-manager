import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const K8sResourceDashboard = ({resourceIds, isActive = true}) => {
  const [parameters, setParameters] = useState([]);
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    const newParameters = [];
    if (resourceIds != null) {
      resourceIds.forEach((resourceId) => {
        newParameters.push({'key': 'var-resourceId', 'value': resourceId.toString()});
      });
      const firstMainResource = resourceIds.values().next().value;
      if (firstMainResource !== 'All') {
        newParameters.push({'key': 'var-mainResourceId', 'value': resourceIds.values().next().value});
      } else {
        newParameters.push({'key': 'var-mainResourceId', 'value': 'None'});
      }
      newParameters.push({'key': 'var-interval', 'value': '2s'});
      newParameters.push({'key': 'refresh', 'value': '2s'});
      setParameters(() => newParameters);
      setParamsInitialized(true);
    }
  }, [resourceIds]);

  if (!paramsInitialized || !isActive) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='b47a9eee-2299-40ec-9aec-ee2e826d3cfd/k8s-dashboard'
      parameters={parameters}
      className="w-full h-[480px]"
    />
  );
};

K8sResourceDashboard.propTypes = {
  resourceIds: PropTypes.instanceOf(Set).isRequired,
  isActive: PropTypes.bool,
};

export default K8sResourceDashboard;
