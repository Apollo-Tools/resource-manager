import {Radio, Select, Space} from 'antd';
import {useState} from 'react';
import SLOValue from './SLOValue';

const {Option} = Select;

const SLOEntry = ({metrics, selectedMetrics, slo, updateMetric, updateExpression, updateValue}) => {
  const [expression, setExpression] = useState('');

  const expressions = {
    number: ['==', '<', '>'],
    boolean: ['=='],
    string: ['=='],
  };

  const onChangeMetric = (value) => {
    updateMetric(value, slo.id);
    if (expressions[slo.metricType].length === 1) {
      onChangeExpression(expressions[slo.metricType][0]);
    } else {
      onChangeExpression('');
    }
  };

  const onChangeExpression = (value) => {
    updateExpression(value, slo.id);
    setExpression(value);
  };

  const metricAlreadySelected = (metric) => {
    return selectedMetrics.find((selectedMetric) => selectedMetric === metric.metric) !== undefined;
  };

  const onChangeValue = (value) => {
    return updateValue(value, slo.id);
  };

  return (
    <Space align="start">
      <Select
        className="w-44"
        showSearch
        placeholder="Select a metric"
        optionFilterProp="children"
        onChange={onChangeMetric}
        size="middle"
      >
        {metrics.map((metric) => (
          <Option key={metric.metric_id} value={metric.metric_id} disabled={metricAlreadySelected(metric)}>
            {metric.metric}
          </Option>
        ))}
      </Select>
      <Radio.Group name="radiogroup" value={expression}
        onChange={(e) => onChangeExpression(e.target.value)} className="w-32"
      >
        {expressions[slo.metricType].map((exp, idx) => {
          return <Radio.Button key={idx} value={exp}>{exp}</Radio.Button>;
        })}
      </Radio.Group>
      <SLOValue expression={expression} metricType={slo.metricType} onChange={onChangeValue}/>
    </Space>
  );
};

export default SLOEntry;
