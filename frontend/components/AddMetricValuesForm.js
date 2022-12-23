import { Button, Checkbox, Form, Select, Input, Space, InputNumber } from 'antd';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { useAuth } from '../lib/AuthenticationProvider';
import { listMetrics } from '../lib/MetricService';
import { addResourceMetrics } from '../lib/ResourceService';

const checkMetricType = (metrics, form, key, metricType) => {
    return metrics
        .find((metric) =>
            metric.metric.metric_id === form
                .getFieldValue('metricValues')[key]?.metric)
        ?.metric.metric_type.type === metricType;
}

const checkMetricIsMonitored = (metrics, form, key) => {
    return metrics
        .find((metric) =>
            metric.metric.metric_id === form
                .getFieldValue('metricValues')[key]?.metric)
        ?.metric.is_monitored;
}

const getInitialMetricValue = (isBoolean) => {
    if (isBoolean) {
        console.log(isBoolean);
        return true;
    }
}

const AddMetricValuesForm = ({ resource, setFinished }) => {
    const [form] = Form.useForm();
    const {token} = useAuth();
    const [metrics, setMetrics] = useState([]);
    const [error, setError] = useState(false);
    Form.useWatch("metricValues", form);

    useEffect(() => {
        listMetrics(token, setMetrics, setError)
            .then(() => {
                setMetrics(prevMetrics => {
                    return prevMetrics.map(metric => {
                        return {metric: metric, isSelected: false}
                    });
                })
            })
    }, [])

    const onFinish = async (values) => {
        let requestBody = values.metricValues.map((metricValue) => {
            console.log(metricValue);
            return {metricId: metricValue.metric, value: metricValue.value ? metricValue.value : false}
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

    return (
        <div className="card container w-full md:w-11/12 max-w-7xl p-10 mt-2 mb-2">
            <h2>Add Metric Values</h2>
            <Form form={form}
                name="basic"
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
                                                    <Select.Option disabled={metric.isSelected} value={metric.metric.metric_id} key={metric.metric.metric_id}>
                                                        {metric.metric.metric}
                                                    </Select.Option>
                                                );
                                            })}

                                        </Select>
                                    </Form.Item>
                                    <Form.Item
                                        {...field}
                                        name={[name, 'value']}
                                        valuePropName={ checkMetricType(metrics, form, key, 'boolean') ?
                                            'checked' : 'value' }
                                        className="w-40"
                                        rules={[
                                            {
                                                required: !checkMetricType(metrics, form, key, 'boolean') &&
                                                    !checkMetricIsMonitored(metrics, form, key),
                                                message: 'Missing value',
                                            },
                                        ]}
                                        // TODO: fix initial value
                                        //hidden={checkMetricIsMonitored(metrics, form, key)}
                                        //initialValue={getInitialMetricValue(checkMetricType(metrics, form, key, 'boolean'))}
                                    >
                                        {checkMetricType(metrics, form, key, 'boolean') ?
                                            <Checkbox className="w-full">
                                                Value
                                            </Checkbox>:
                                            checkMetricType(metrics, form, key, 'number') ?
                                                <InputNumber placeholder='0.00' controls={false} className="w-full"/>:
                                                <Input placeholder='value' className="w-full"/>

                                        }

                                    </Form.Item>
                                    <MinusCircleOutlined onClick={() => onRemoveMetricValue(remove, name)} />
                                </Space>
                            ))}
                            <Form.Item>
                                <Button type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                                    Add Metric Value
                                </Button>
                            </Form.Item>
                        </>
                    )}
                </Form.List>

                <div className="flex">
                    <Form.Item>
                        <Button type="primary" htmlType="submit">
                            Create
                        </Button>
                    </Form.Item>
                    <div className="flex-1"/>
                    <Form.Item>
                        <Button type="default" onClick={onClickSkip}>
                            Skip
                        </Button>
                    </Form.Item>
                </div>
            </Form>
        </div>
    )
}

export default AddMetricValuesForm;