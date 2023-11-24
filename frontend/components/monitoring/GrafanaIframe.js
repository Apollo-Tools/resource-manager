import env from '@beam-australia/react-env';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const GrafanaIframe = ({dashboardId, parameters = new Map(), className}) => {
  const [parameterString, setParameterString] = useState('');

  useEffect(() => {
    setParameterString(() => {
      return Array.from(parameters.entries())
          .map(([key, value]) => `${key}=${encodeURIComponent(value)}`)
          .join('&');
    });
  }, [parameters]);

  return (
    <iframe
      src={`${env('GRAFANA_URL')}/d/${dashboardId}?orgId=1&kiosk&${parameterString}`}
      className={'border-0 ' + className}
    >
    </iframe>
  );
};

GrafanaIframe.propTypes = {
  dashboardId: PropTypes.string.isRequired,
  parameters: PropTypes.instanceOf(Map).isRequired,
  className: PropTypes.string,
};

export default GrafanaIframe;
