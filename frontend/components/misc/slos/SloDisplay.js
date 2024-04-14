import {Divider} from 'antd';
import PropTypes from 'prop-types';
import {useEffect, useState} from 'react';
import {useAuth} from '../../../lib/misc/AuthenticationProvider';
import {listRegions} from '../../../lib/api/RegionService';
import {listResourceTypes} from '../../../lib/api/ResourceTypeService';
import {listResourceProviders} from '../../../lib/api/ResourceProviderService';
import {listPlatforms} from '../../../lib/api/PlatformService';
import {listEnvironments} from '../../../lib/api/EnvironmentService';
import BoolValueDisplay from '../BoolValueDisplay';


const SloDisplay = ({slos, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [regions, setRegions] = useState([]);
  const [providers, setProviders] = useState([]);
  const [resourceTypes, setResourceTypes] = useState([]);
  const [environments, setEnvironments] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [isLoading, setLoading] = useState(false);

  useEffect(() => {
    if (slos && !checkTokenExpired()) {
      const sloNames = new Set(slos.map((slo) => slo.name));
      if (sloNames.has('region')) {
        void listRegions(token, setRegions, setLoading, setError);
      }
      if (sloNames.has('resource_type')) {
        void listResourceTypes(token, setResourceTypes, setLoading, setError);
      }
      if (sloNames.has('platform')) {
        void listPlatforms(token, setPlatforms, setLoading, setError);
      }
      if (sloNames.has('resource_provider')) {
        void listResourceProviders(token, setProviders, setLoading, setError);
      }
      if (sloNames.has('environment')) {
        void listEnvironments(token, setEnvironments, setLoading, setError);
      }
    }
  }, [slos]);

  const mapSloValue = (name, value) => {
    switch (name) {
      case 'region':
        return regions.find((region) => region.region_id === value)?.name ?? value;
      case 'resource_type':
        return resourceTypes.find((type) => type.type_id === value)?.resource_type ?? value;
      case 'platform':
        return platforms.find((platform) => platform.platform_id === value)?.platform ?? value;
      case 'resource_provider':
        return providers.find((provider) => provider.provider_id === value)?.provider ?? value;
      case 'environment':
        return environments.find((environment) => environment.environment_id === value)?.environment ?? value;
      default:
        if (typeof value == 'boolean') {
          return <BoolValueDisplay value={value} colored={false}/>;
        }
        return value.toString();
    }
  };

  return <>
    {slos.map((slo, idx) => {
      return (
        <div key={idx}>
          <div className="grid grid-cols-12 gap-4">
            <span className="shadow-sky-300 shadow-md m-1 bg-sky-800 text-gray-200 w-full rounded-full inline-flex h-6 col-span-2 place-self-center">
              <div className="m-auto font-bold">{slo.name}</div>
            </span>
            <span className="shadow-sky-300 shadow-md m-1 bg-secondary text-gray-200 rounded-full inline-flex w-10 h-7 col-span-1 place-self-center">
              <div className="m-auto font-serif font-extrabold" >{slo.expression}</div>
            </span>
            <span className="m-1 w-full col-span-2 place-self-center">{slo.value.map(((value, idx2) => {
              return <div key={idx2} className="shadow-cyan-200 shadow-md m-1 bg-cyan-600 text-gray-200 w-48 rounded-full flex h-6 ">
                <div className="m-auto font-extrabold">
                  {mapSloValue(slo.name, value)}
                </div>
              </div>;
            }))}</span>
          </div>
          <Divider className="m-1"/>
        </div>);
    })}
  </>;
};

SloDisplay.propTypes = {
  slos: PropTypes.arrayOf(PropTypes.object).isRequired,
  setError: PropTypes.func.isRequired,
};

export default SloDisplay;
