import {Button, Checkbox, Form, Select, Input, InputNumber, Typography} from 'antd';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {listPlatformMetrics} from '../../lib/PlatformMetricService';
import {addResourceMetrics} from '../../lib/ResourceService';
import PropTypes from 'prop-types';
import TooltipIcon from '../misc/TooltipIcon';

const getMetricById = (metrics, metricId) => {
  return metrics
      .find((metric) =>
        metric.metric.metric_id === metricId);
};

const checkMetricType = (metrics, form, name, metricType) => {
  return getMetricById(metrics,
      form.getFieldValue('metricValues')[name]?.metric)
      ?.metric.metric_type.type === metricType;
};


const getMetricDescription = (metrics, form, name) => {
  return getMetricById(metrics,
      form.getFieldValue('metricValues')[name]?.metric)
      ?.metric.description;
};

const checkMetricIsMonitored = (metrics, form, name) => {
  return getMetricById(metrics,
      form.getFieldValue('metricValues')[name]?.metric)
      ?.metric.is_monitored;
};

const checkMetricIsSelected = (metrics, form, name) => {
  return getMetricById(metrics, form.getFieldValue('metricValues')[name]?.metric) != null;
};

const AddMetricValuesForm = ({
  resource,
  excludeMetricIds,
  setFinished,
}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [error, setError] = useState(false);
  const [requiredSelected, setRequiredSelected] = useState(false);
  Form.useWatch('basic', form);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listPlatformMetrics(resource.platform.platform_id, token, setMetrics, setError)
          .then(() => {
            setMetrics((prevMetrics) => {
              let filteredMetrics = prevMetrics;
              if (excludeMetricIds != null) {
                filteredMetrics = prevMetrics
                    .filter((metric) => !excludeMetricIds
                        .includes(metric.metric.metric_id));
              }
              return filteredMetrics
                  .map((metric) => {
                    return {metric: metric.metric, required: metric.required, isSelected: false};
                  });
            });
          });
    }
  }, [excludeMetricIds]);

  useEffect(() => {
    setRequiredSelected(metrics
        .filter((metric) => metric.required && !metric.isSelected)
        .length === 0);
  }, [metrics]);

  const onFinish = async (values) => {
    if (checkTokenExpired()) return;
    const requestBody = values.metricValues.map((metricValue) => {
      const metric = metrics.find((metric) =>
        metric.metric.metric_id === metricValue.metric).metric;
      if (metric.is_monitored) {
        return {metricId: metricValue.metric};
      } else {
        return {
          metricId: metricValue.metric,
          value: metric.metric_type.type === 'boolean' && metricValue.value === undefined ? false :
                        metricValue.value};
      }
    });
    await addResourceMetrics(resource.resource_id, requestBody, token, setError)
        .then(() => {
          if (!error) {
            setFinished(true);
            form.resetFields();
          }
        });
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onChangeMetric = () => {
    const selectedMetrics = form.getFieldValue('metricValues')
        .filter((metric) => metric != null)
        .map((metric) => metric.metric);
    setMetrics((prevMetrics) => {
      return prevMetrics.map((metric) => {
        const isSelected = selectedMetrics.includes(metric.metric.metric_id);
        return {metric: metric.metric, required: metric.required, isSelected: isSelected};
      });
    });
  };

  const onRemoveMetricValue = (remove, name) => {
    remove(name);
    onChangeMetric();
  };

  if (metrics.length === 0) {
    return (<></>);
  }

  return (
    <>
      <Typography.Title level={3}>Add Metric Values</Typography.Title>
      <Form form={form}
        name="metricValueForm"
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
      >
        <Form.List name="metricValues">
          {(fields, {add, remove}) => (
            <div className="grid grid-cols-1 content-between">
              {fields.map(({index, key, name, ...field}) => (
                <div
                  key={key}
                  className="flex gap-2 content-center py-2"
                >
                  <Form.Item
                    {...field}
                    name={[name, 'metric']}
                    rules={[
                      {
                        required: true,
                        message: 'Missing metric',
                      },
                    ]}
                    className="w-40 mb-0"
                  >
                    <Select className="w-40" placeholder="Metric" onChange={onChangeMetric}>
                      {metrics.map((metric) => {
                        return (
                          <Select.Option disabled={metric.isSelected}
                            value={metric.metric.metric_id}
                            key={metric.metric.metric_id}
                          >
                            {metric.required ? metric.metric.metric + ' *' : metric.metric.metric}
                          </Select.Option>
                        );
                      })}
                    </Select>
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[name, 'value']}
                    valuePropName={ checkMetricType(metrics, form, name, 'boolean') ?
                                            'checked' : 'value' }
                    className="w-40 mb-0"
                    rules={[
                      {
                        required: !checkMetricType(metrics, form, name, 'boolean') &&
                                                    !checkMetricIsMonitored(metrics, form, name),
                        message: 'Missing value',
                      },
                    ]}
                    hidden={checkMetricIsMonitored(metrics, form, name) ||
                                            !checkMetricIsSelected(metrics, form, name)}
                  >
                    {checkMetricType(metrics, form, name, 'boolean') ?
                      <Checkbox className="w-full">
                          Value
                      </Checkbox>:
                      (checkMetricType(metrics, form, name, 'number') ?
                          <InputNumber placeholder='0.00' controls={false} className="w-full"/>:
                          <Input placeholder='value' classNames="w-full"/>
                      )

                    }

                  </Form.Item>
                  { checkMetricIsSelected(metrics, form, name) &&
                    <TooltipIcon text={getMetricDescription(metrics, form, name)}/>
                  }
                  <MinusCircleOutlined onClick={() => onRemoveMetricValue(remove, name)}/>
                </div>
              ))}
              <Form.Item>
                <Button disabled={metrics.length === 0 ||
                                    form.getFieldValue('metricValues')?.length >= metrics.length}
                type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                                    Add Metric Value
                </Button>
              </Form.Item>
            </div>
          )}
        </Form.List>

        <div className="flex">
          <Form.Item>
            <Button type="primary" htmlType="submit"
              disabled={form.getFieldValue('metricValues') == null ||
                        form.getFieldValue('metricValues')?.length <= 0 ||
                        !requiredSelected}>
              Create
            </Button>
          </Form.Item>
          <div className="flex-1"/>
        </div>
      </Form>
    </>
  );
};

AddMetricValuesForm.propTypes = {
  resource: PropTypes.object.isRequired,
  excludeMetricIds: PropTypes.arrayOf(PropTypes.number.isRequired),
  setFinished: PropTypes.func.isRequired,
};

export default AddMetricValuesForm;
