import {useEffect, useState} from 'react';
import SLOEntry from './SLOEntry';
import {Button} from 'antd';
import {CloseSquareOutlined, PlusSquareOutlined} from '@ant-design/icons';
import {listMetrics} from '../../../lib/api/MetricService';
import {useAuth} from '../../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import {listRegions} from '../../../lib/api/RegionService';
import {listResourceTypes} from '../../../lib/api/ResourceTypeService';
import {listPlatforms} from '../../../lib/api/PlatformService';
import {listResourceProviders} from '../../../lib/api/ResourceProviderService';
import {listEnvironments} from '../../../lib/api/EnvironmentService';
import {updateLoadingState} from '../../../lib/misc/LoadingUtil';
import LoadingSpinner from '../LoadingSpinner';


const SLOSelection = ({onChange, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [regions, setRegions] = useState([]);
  const [providers, setProviders] = useState([]);
  const [resourceTypes, setResourceTypes] = useState([]);
  const [environments, setEnvironments] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [sloId, setSloId] = useState(0);
  const [isLoading, setLoading] = useState(
      {
        'listMetrics': true,
        'listRegions': true,
        'listResourceTypes': true,
        'listPlatforms': true,
        'listResourceProviders': true,
        'listEnvironments': true,
      });
  const [metricsInitialised, setMetricsInitialised] = useState(false);
  const [slos, setSlos] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listMetrics(token, setMetrics, updateLoadingState('listMetrics', setLoading), setError);
      void listRegions(token, setRegions, updateLoadingState('listRegions', setLoading), setError);
      void listResourceTypes(token, setResourceTypes, updateLoadingState('listResourceTypes', setLoading), setError);
      void listPlatforms(token, setPlatforms, updateLoadingState('listPlatforms', setLoading), setError);
      void listResourceProviders(token, setProviders, updateLoadingState('listResourceProviders', setLoading), setError);
      void listEnvironments(token, setEnvironments, updateLoadingState('listEnvironments', setLoading), setError);
    }
  }, []);

  useEffect(() => {
    if (metrics.length > 0 && !metricsInitialised) {
      const onlySLOMetrics = metrics.filter((metric) => metric.is_slo);
      const includingNonMetricSLOs = [...onlySLOMetrics,
        {metric_id: -1, metric: 'region', metric_type: {type: 'number'}, description: 'the' +
            ' cloud/edge region'},
        {metric_id: -2, metric: 'resource_provider', metric_type: {type: 'number'}, description: 'the resource' +
              ' provider'},
        {metric_id: -3, metric: 'resource_type', metric_type: {type: 'number'}, description: 'the resource type'},
        {metric_id: -4, metric: 'environment', metric_type: {type: 'number'}, description: 'the deployment' +
              ' environment'},
        {metric_id: -5, metric: 'platform', metric_type: {type: 'number'}, description: 'the deployment platform'}];
      setMetrics(includingNonMetricSLOs.sort((slo1, slo2) => slo1.metric.localeCompare(slo2.metric)));
      setMetricsInitialised(true);
    }
  }, [metrics]);

  const triggerChange = (changedValue) => {
    onChange?.(changedValue);
  };


  useEffect(() => {
    triggerChange(slos);
  }, [slos]);

  const onClickAddSLO = () => {
    setSlos((prevSlos) => [...prevSlos, {
      id: sloId,
      name: '',
      metricType: 'number',
      expression: '',
      value: [],
    }]);
    setSloId((prevId) => prevId + 1);
  };

  const onClickRemoveSLO = (id) => {
    setSlos((prevSlos) => prevSlos.filter((slo) => slo.id !== id));
  };


  const onChangeMetricSelect = (value, sloId) => {
    const metric = metrics.find((metric) => metric.metric_id === value);
    setSlos((prevSlos) => {
      return prevSlos.map((slo) => {
        if (slo.id === sloId) {
          slo.name = metric?.metric;
          slo.metricType = metric?.metric_type?.type;
        }
        return slo;
      });
    });
  };

  const onChangeExpressionSelect = (value, sloId) => {
    setSlos((prevSlos) => {
      return prevSlos.map((slo) => {
        if (slo.id === sloId) {
          slo.expression = value;
        }
        return slo;
      });
    });
  };

  const onChangeValue = (value, sloId) => {
    setSlos((prevSlos) => {
      return prevSlos.map((slo) => {
        if (slo.id === sloId) {
          slo.value = value;
        }
        return slo;
      });
    });
  };

  const getSelectables = (sloName) => {
    let selectables;
    switch (sloName) {
      case 'region':
        selectables = regions.map((region) => {
          return {id: region.region_id, name: region.name};
        });
        break;
      case 'resource_type':
        selectables = resourceTypes.map((type) => {
          return {id: type.type_id, name: type.resource_type};
        });
        break;
      case 'platform':
        selectables = platforms.map((platform) => {
          return {id: platform.platform_id, name: platform.platform};
        });
        break;
      case 'resource_provider':
        selectables = providers.map((provider) => {
          return {id: provider.provider_id, name: provider.provider};
        });
        break;
      case 'environment':
        selectables = environments.map((environment) => {
          return {id: environment.environment_id, name: environment.environment};
        });
        break;
      default:
        return null;
    }
    return selectables.sort((sel1, sel2) =>
      sel1.name.localeCompare(sel2.name));
  };

  if (Object.values(isLoading).some((a) => a)) {
    return <LoadingSpinner isCard={false} />;
  }

  return (<div>
    {slos.map((slo) => {
      return (
        <div key={slo.id}>
          <SLOEntry
            metrics={metrics}
            selectedMetrics={slos.map((metricSlo) => metricSlo.name)}
            slo={slo}
            selectables={getSelectables(slo.name)}
            updateMetric={onChangeMetricSelect}
            updateExpression={onChangeExpressionSelect}
            updateValue={onChangeValue}
          />
          <Button type="ghost" icon={<CloseSquareOutlined />} onClick={() => onClickRemoveSLO(slo.id)}/>
        </div>
      );
    })}
    <Button className="mt-2" type="default" icon={<PlusSquareOutlined />} onClick={onClickAddSLO}>SLO</Button>
  </div>);
};

SLOSelection.propTypes = {
  onChange: PropTypes.func,
  setError: PropTypes.func.isRequired,
};

export default SLOSelection;
