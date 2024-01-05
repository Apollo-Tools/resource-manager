import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const InvocationDashboard = ({resourceIds, isActive = true}) => {
  const [parameters] = useState(new Map());
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    if (resourceIds != null) {
      resourceIds.forEach((resourceId) => {
        parameters.set('var-resourceId', resourceId.toString());
      });
      parameters.set('var-interval', '2s');
      parameters.set('var-job', 'apollo-rm-scrape');
      parameters.set('refresh', '1s');
      console.log(parameters);
      setParamsInitialized(true);
    }
  }, [resourceIds]);

  if (!paramsInitialized || !isActive) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='efa71f23-5850-4144-8b29-0ae9480eec94'
      parameters={parameters}
      className="w-full h-[450px]"
    />
  );
};

InvocationDashboard.propTypes = {
  resourceIds: PropTypes.arrayOf(PropTypes.number).isRequired,
  isActive: PropTypes.bool,
};

export default InvocationDashboard;
