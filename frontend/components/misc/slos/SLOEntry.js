import {Radio, Select, Space} from 'antd';
import {useState} from 'react';
import SLOValue from './SLOValue';
import PropTypes from 'prop-types';
import TooltipIcon from '../TooltipIcon';

const {Option} = Select;

const SLOEntry = ({metrics, selectedMetrics, slo, selectables, updateMetric, updateExpression, updateValue}) => {
  const [expression, setExpression] = useState('');
  const [selectedMetric, setSelectedMetric] = useState();

  const expressions = {
    number: ['==', '<', '>'],
    boolean: ['=='],
    string: ['=='],
    selectable: ['=='],
  };

  const onChangeMetric = (value) => {
    updateMetric?.(value, slo.id);
    setSelectedMetric(metrics.find((metric) => metric.metric_id === value));
    onChangeExpression('');
  };

  const onChangeExpression = (value) => {
    updateExpression?.(value, slo.id);
    setExpression(value);
  };

  const metricAlreadySelected = (metric) => {
    return selectedMetrics.find((selectedMetric) => selectedMetric === metric.metric) !== undefined;
  };

  const onChangeValue = (value) => {
    return updateValue?.(value, slo.id);
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
      <span className='flex content-center h-[32px] w-[23px]'>
        {selectedMetric != null && <TooltipIcon text={selectedMetric?.description}/>}
      </span>
      <Radio.Group name="radiogroup" value={expression}
        onChange={(e) => onChangeExpression(e.target.value)} className="w-32"
      >
        {(selectables ? expressions.selectable : expressions[slo.metricType]).map((exp, idx) => {
          return <Radio.Button key={idx} value={exp}>{exp}</Radio.Button>;
        })}
      </Radio.Group>
      <SLOValue expression={expression} metricType={selectables ? 'selectable' : slo.metricType} onChange={onChangeValue}
        selectables={selectables}/>
    </Space>
  );
};

SLOEntry.propTypes = {
  metrics: PropTypes.arrayOf(PropTypes.object).isRequired,
  selectedMetrics: PropTypes.arrayOf(PropTypes.string).isRequired,
  slo: PropTypes.object.isRequired,
  selectables: PropTypes.arrayOf(PropTypes.shape({id: PropTypes.number.isRequired, name: PropTypes.string.isRequired})),
  updateMetric: PropTypes.func,
  updateExpression: PropTypes.func,
  updateValue: PropTypes.func,
};

export default SLOEntry;
