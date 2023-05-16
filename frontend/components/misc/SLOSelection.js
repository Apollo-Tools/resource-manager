import {useEffect, useState} from 'react';
import SLOEntry from './SLOEntry';
import {Button} from 'antd';
import {CloseSquareOutlined, PlusSquareOutlined} from '@ant-design/icons';
import {listMetrics} from '../../lib/MetricService';
import {useAuth} from '../../lib/AuthenticationProvider';


const SLOSelection = ({value = {}, onChange}) => {
  const {token, checkTokenExpired} = useAuth();
  const [metrics, setMetrics] = useState([]);
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
    }
  }, []);

  useEffect(() => {
    console.log(metrics);
    if (metrics.length > 0 && !metricsInitialised) {
      console.log('init metrics');
      setMetrics((prevMetrics) =>
        [...prevMetrics, {metric_id: -1, metric: 'region', metric_type: {type: 'number'}},
          {metric_id: -2, metric: 'resource_provider', metric_type: {type: 'number'}},
          {metric_id: -3, metric: 'resource_type', metric_type: {type: 'number'}}]);
      setMetricsInitialised(true);
    }
  }, [metrics]);

  const triggerChange = (changedValue) => {
    onChange?.({
      slos,
      ...value,
      ...changedValue,
    });
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
    console.log(id);
    setSlos((prevSlos) => prevSlos.filter((slo) => slo.id !== id));
  };


  const onChangeMetricSelect = (value, sloId) => {
    const metric = metrics.find((metric) => metric.metric_id === value);
    console.log(metric);
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


  return (<div>
    {slos.map((slo) => {
      return (
        <div key={slo.id}>
          <SLOEntry
            metrics={metrics}
            selectedMetrics={slos.map((metricSlo) => metricSlo.name)}
            slo={slo}
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

export default SLOSelection;
