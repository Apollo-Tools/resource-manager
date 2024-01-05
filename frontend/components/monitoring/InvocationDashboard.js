import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const InvocationDashboard = ({deploymentId, isActive = true}) => {
  const [parameters] = useState(new Map());
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    if (deploymentId != null) {
      parameters.set('var-deploymentId', deploymentId.toString());
      if (isActive) {
        parameters.set('refresh', '5s');
      }
      setParamsInitialized(true);
    }
  }, [deploymentId]);

  if (!paramsInitialized) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='db738643-24c0-4975-8da7-76a2259e9479/deployment'
      parameters={parameters}
      className="w-full h-[710px]"
    />
  );
};

InvocationDashboard.propTypes = {
  deploymentId: PropTypes.number.isRequired,
  isActive: PropTypes.bool,
};

export default InvocationDashboard;
