import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const K8sPodDashboard = ({deploymentId, isActive = true}) => {
  const [parameters, setParameters] = useState([]);
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    const newParameters = [];
    if (deploymentId != null) {
      newParameters.push({'key': 'var-deploymentId', 'value': deploymentId});
      newParameters.push({'key': 'refresh', 'value': '5s'});
      setParameters(() => newParameters);
      setParamsInitialized(true);
    }
  }, [deploymentId]);

  if (!paramsInitialized || !isActive) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='e1538e55-0282-449a-b1f4-e4c733f25f2d/k8s-pods'
      parameters={parameters}
      className="w-full h-[480px]"
    />
  );
};

K8sPodDashboard.propTypes = {
  deploymentId: PropTypes.number.isRequired,
  isActive: PropTypes.bool,
};

export default K8sPodDashboard;
