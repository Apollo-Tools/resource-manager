import env from '@beam-australia/react-env';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';

const GrafanaIframe = ({dashboardId, parameters = [], className}) => {
  const [parameterString, setParameterString] = useState('');

  useEffect(() => {
    setParameterString(() => {
      return parameters
          .map((parameter) => `${parameter.key}=${encodeURIComponent(parameter.value)}`)
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
  parameters: PropTypes.arrayOf(
      PropTypes.shape({
        key: PropTypes.number.isRequired,
        value: PropTypes.string.isRequired,
      }),
  ).isRequired,
  className: PropTypes.string,
};

export default GrafanaIframe;
