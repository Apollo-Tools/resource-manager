import GrafanaIframe from './GrafanaIframe';
import {useEffect, useState} from 'react';

const RegionDashboard = () => {
  const [parameters, setParameters] = useState([]);
  const [paramsInitialized, setParamsInitialized] = useState(false);

  useEffect(() => {
    const newParameters = [];
    newParameters.push({'key': 'refresh', 'value': '5s'});
    setParameters(() => newParameters);
    setParamsInitialized(true);
  }, []);

  if (!paramsInitialized) {
    return <></>;
  }

  return (
    <GrafanaIframe dashboardId='e32c5920-4a78-46e5-a5a1-c78f9ba640a7/region-dashboard'
      parameters={parameters}
      className="w-full h-[480px]"
    />
  );
};

export default RegionDashboard;
