import {useEffect, useState} from 'react';
import SLOEntry from './SLOEntry';
import {Button} from 'antd';
import {CloseSquareOutlined, PlusSquareOutlined} from '@ant-design/icons';
import {listMetrics} from '../../../lib/MetricService';
import {useAuth} from '../../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {listRegions} from '../../../lib/RegionService';
import {listResourceTypes} from '../../../lib/ResourceTypeService';
import {listPlatforms} from '../../../lib/PlatformService';
import {listResourceProviders} from '../../../lib/ResourceProviderService';
import {listEnvironments} from '../../../lib/EnvironmentService';


const SLOSelection = ({onChange}) => {
  const {token, checkTokenExpired} = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [regions, setRegions] = useState([]);
  const [providers, setProviders] = useState([]);
  const [resourceTypes, setResourceTypes] = useState([]);
  const [environments, setEnvironments] = useState([]);
  const [platforms, setPlatforms] = useState([]);
  const [sloId, setSloId] = useState(0);
  const [error, setError] = useState();
  const [metricsInitialised, setMetricsInitialised] = useState(false);
  const [slos, setSlos] = useState([]);


  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listMetrics(token, setMetrics, setError);
      listRegions(token, setRegions, setError);
      listResourceTypes(token, setResourceTypes, setError);
      listPlatforms(token, setPlatforms, setError);
      listResourceProviders(token, setProviders, setError);
      listEnvironments(token, setEnvironments, setError);
    }
  }, []);

  useEffect(() => {
    if (metrics.length > 0 && !metricsInitialised) {
      setMetrics((prevMetrics) =>
        [...prevMetrics, {metric_id: -1, metric: 'region', metric_type: {type: 'number'}, description: 'the' +
            ' cloud/edge region'},
        {metric_id: -2, metric: 'resource_provider', metric_type: {type: 'number'}, description: 'the resource' +
              ' provider'},
        {metric_id: -3, metric: 'resource_type', metric_type: {type: 'number'}, description: 'the resource type'},
        {metric_id: -4, metric: 'environment', metric_type: {type: 'number'}, description: 'the deployment' +
              ' environment'},
        {metric_id: -5, metric: 'platform', metric_type: {type: 'number'}, description: 'the deployment platform'}]);
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
    switch (sloName) {
      case 'region':
        return regions.map((region) => {
          return {id: region.region_id, name: region.name};
        });
      case 'resource_type':
        return resourceTypes.map((type) => {
          return {id: type.type_id, name: type.resource_type};
        });
      case 'platform':
        return platforms.map((platform) => {
          return {id: platform.platform_id, name: platform.platform};
        });
      case 'resource_provider':
        return providers.map((provider) => {
          return {id: provider.provider_id, name: provider.provider};
        });
      case 'environment':
        return environments.map((environment) => {
          return {id: environment.environment_id, name: environment.environment};
        });
      default:
        return null;
    }
  };


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
};

export default SLOSelection;
