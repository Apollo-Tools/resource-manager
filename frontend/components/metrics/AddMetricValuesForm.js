import {Button, Checkbox, Form, Select, Input, InputNumber, Typography} from 'antd';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {listPlatformMetrics} from '../../lib/api/PlatformMetricService';
import {addResourceMetrics} from '../../lib/api/ResourceService';
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

const checkMetricIsSelected = (metrics, form, name) => {
  return getMetricById(metrics, form.getFieldValue('metricValues')[name]?.metric) != null;
};

const AddMetricValuesForm = ({resource, excludeMetricIds, setFinished, isNewResource, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [metrics, setMetrics] = useState([]);
  const [filteredMetrics, setFilteredMetrics] = useState([]);
  const [isLoading, setLoading] = useState(false);
  const [requiredSelected, setRequiredSelected] = useState(false);
  Form.useWatch('basic', form);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listPlatformMetrics(resource.platform.platform_id, token, setMetrics, setLoading, setError);
    }
  }, [excludeMetricIds]);

  useEffect(() => {
    setFilteredMetrics(() => {
      const filtered = metrics
          .filter((metric) => {
            const includeMetric = excludeMetricIds != null ?
              !excludeMetricIds.includes(metric.metric.metric_id) : true;
            if (resource == null || !isNewResource || !Object.hasOwn(resource, 'main_resource_id')) {
              return metric.is_main_resource_metric && includeMetric && !metric.is_monitored;
            } else {
              return metric.is_sub_resource_metric && includeMetric && !metric.is_monitored;
            }
          })
          .sort((a, b) => a.metric.metric.localeCompare(b.metric.metric));
      return filtered
          .map((metric) => {
            return {
              metric: metric.metric,
              is_main_resource_metric: metric.is_main_resource_metric,
              is_sub_resource_metric: metric.is_sub_resource_metric,
              isSelected: false};
          });
    });
  }, [metrics]);

  useEffect(() => {
    setRequiredSelected(filteredMetrics
        .filter((metric) => !metric.isSelected)
        .length === 0);
  }, [filteredMetrics]);

  const onFinish = async (values) => {
    if (checkTokenExpired()) return;
    const requestBody = values.metricValues.map((metricValue) => {
      const platformMetric = getMetricById(metrics, metricValue.metric);
      if (platformMetric.is_monitored) {
        return {metric_id: metricValue.metric};
      } else {
        return {
          metric_id: metricValue.metric,
          value: platformMetric.metric.metric_type.type === 'boolean' && metricValue.value === undefined ? false :
                        metricValue.value};
      }
    });
    await addResourceMetrics(resource.resource_id, requestBody, token, setLoading, setError)
        .then((result) => {
          if (result) {
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
    setFilteredMetrics((prevMetrics) => {
      return prevMetrics.map((metric) => {
        const isSelected = selectedMetrics.includes(metric.metric.metric_id);
        return {
          metric: metric.metric,
          is_main_resource_metric: metric.is_main_resource_metric,
          is_sub_resource_metric: metric.is_sub_resource_metric,
          isSelected: isSelected};
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
                      {filteredMetrics.map((metric) => {
                        return (
                          <Select.Option disabled={metric.isSelected}
                            value={metric.metric.metric_id}
                            key={metric.metric.metric_id}
                          >
                            {metric.metric.metric}
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
                        required: !checkMetricType(metrics, form, name, 'boolean'),
                        message: 'Missing value',
                      },
                    ]}
                    hidden={!checkMetricIsSelected(metrics, form, name)}
                  >
                    {checkMetricType(metrics, form, name, 'boolean') ?
                      <Checkbox className="w-full">
                          Value
                      </Checkbox>:
                      (checkMetricType(metrics, form, name, 'number') ?
                          <InputNumber placeholder='0.00' controls={false} className="w-full"/>:
                          <Input placeholder='value' className="w-full"/>
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
                <Button disabled={filteredMetrics.length === 0 ||
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
  isNewResource: PropTypes.bool,
  setError: PropTypes.func.isRequired,
};

export default AddMetricValuesForm;
