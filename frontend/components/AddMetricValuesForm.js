import { Button, Checkbox, Form, Select, Input, Space, InputNumber } from 'antd';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { useAuth } from '../lib/AuthenticationProvider';
import { listMetrics } from '../lib/MetricService';
import { addResourceMetrics } from '../lib/ResourceService';

const getMetricById = (metrics, metricId) => {
    return metrics
        .find((metric) =>
            metric.metric.metric_id === metricId)
}

const checkMetricType = (metrics, form, name, metricType) => {
    return getMetricById(metrics, form.getFieldValue('metricValues')[name]?.metric)
        ?.metric.metric_type.type === metricType;
}

const checkMetricIsMonitored = (metrics, form, name) => {
    return getMetricById(metrics, form.getFieldValue('metricValues')[name]?.metric)
        ?.metric.is_monitored;
}

const checkMetricIsSelected = (metrics, form, name) => {
    return getMetricById(metrics, form.getFieldValue('metricValues')[name]?.metric) != null;
}

const AddMetricValuesForm = ({ resource, excludeMetricIds, setFinished, isSkipable }) => {
    const [form] = Form.useForm();
    const {token, checkTokenExpired} = useAuth();
    const [metrics, setMetrics] = useState([]);
    const [error, setError] = useState(false);
    Form.useWatch("basic", form);

    useEffect(() => {
        if (!checkTokenExpired()) {
            listMetrics(token, setMetrics, setError)
                .then(() => {
                    setMetrics(prevMetrics => {
                        let filteredMetrics = prevMetrics;
                        if (excludeMetricIds != null) {
                            filteredMetrics = prevMetrics
                                .filter(metric => !excludeMetricIds.includes(metric.metric_id));
                        }
                        console.log(filteredMetrics);
                        return filteredMetrics
                            .map(metric => {
                            return {metric: metric, isSelected: false}
                        });
                    })
                })
        }
    }, [excludeMetricIds])

    const onFinish = async (values) => {
        console.log(values);
        if (checkTokenExpired()) return;
        let requestBody = values.metricValues.map((metricValue) => {
            console.log(metricValue);
            let metric = metrics.find((metric) => metric.metric.metric_id === metricValue.metric).metric
            if (metric.is_monitored) {
                return {metricId: metricValue.metric}
            } else {
                return {
                    metricId: metricValue.metric,
                    value: metric.metric_type.type === 'boolean' && metricValue.value === undefined ? false :
                        metricValue.value }
            }
        })
        await addResourceMetrics(resource.resource_id, requestBody, token, setError)
            .then(() => {
                if(!error) setFinished(true)
            });
    };

    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

    const onChangeMetric = () => {
        let selectedMetrics = form.getFieldValue('metricValues')
            .filter(metric => metric != null)
            .map(metric => metric.metric);
        setMetrics(prevMetrics => {
            return prevMetrics.map(metric => {
                let isSelected = selectedMetrics.includes(metric.metric.metric_id);
                return {metric: metric.metric, isSelected: isSelected}
            });
        })
    }

    const onRemoveMetricValue = (remove, name) => {
        remove(name);
        onChangeMetric();
    }

    const onClickSkip = () => {
        setFinished(true);
    }

    if (metrics.length === 0) {
        return (<></>);
    }

    return (
        <>
            <h2>Add Metric Values</h2>
            <Form form={form}
                name="metricValueForm"
                onFinish={onFinish}
                onFinishFailed={onFinishFailed}
                autoComplete="off"
            >
                <Form.List name="metricValues">
                    {(fields, { add, remove }) => (
                        <>
                            {fields.map(({index, key, name, ...field}) => (
                                <Space
                                    key={key}
                                    className="flex"
                                    align="baseline"
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
                                    >
                                        <Select className="w-40" placeholder="Metric" onChange={onChangeMetric}>
                                            {metrics.map(metric => {
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
                                        className="w-40"
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
                                            checkMetricType(metrics, form, name, 'number') ?
                                                <InputNumber placeholder='0.00' controls={false} className="w-full"/>:
                                                <Input placeholder='value' className="w-full"/>

                                        }

                                    </Form.Item>
                                    <MinusCircleOutlined onClick={() => onRemoveMetricValue(remove, name)} />
                                </Space>
                            ))}
                            <Form.Item>
                                <Button disabled={metrics.length === 0 ||
                                    form.getFieldValue("metricValues")?.length >= metrics.length}
                                        type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                                    Add Metric Value
                                </Button>
                            </Form.Item>
                        </>
                    )}
                </Form.List>

                <div className="flex">
                    <Form.Item>
                        <Button type="primary" htmlType="submit"
                                disabled={form.getFieldValue("metricValues") == null ||
                                    form.getFieldValue("metricValues")?.length <= 0}>
                            Create
                        </Button>
                    </Form.Item>
                    <div className="flex-1"/>
                    <Form.Item hidden={!isSkipable}>
                        <Button type="default" onClick={onClickSkip}>
                            Skip
                        </Button>
                    </Form.Item>
                </div>
            </Form>
        </>
    )
}

export default AddMetricValuesForm;