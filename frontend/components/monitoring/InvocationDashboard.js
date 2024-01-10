import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const InvocationDashboard = ({deploymentId, isActive = true}) => {
  const [parameters, setParameters] = useState([]);
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    const newParameters = [];
    if (deploymentId != null) {
      newParameters.push({'key': 'var-deploymentId', 'value': deploymentId.toString()});
      if (isActive) {
        newParameters.push({'key': 'refresh', 'value': '5s'});
      }
      setParameters(() => newParameters);
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
