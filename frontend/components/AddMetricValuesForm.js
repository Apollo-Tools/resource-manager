import { Button, Checkbox, Form, Select, Input, Space, InputNumber } from 'antd';
import { MinusCircleOutlined, PlusOutlined } from '@ant-design/icons';
import { useEffect, useState } from 'react';
import { useAuth } from '../lib/AuthenticationProvider';
import { listMetrics } from '../lib/MetricService';

const AddMetricValuesForm = ({ resource }) => {
    const [form] = Form.useForm();
    const {token} = useAuth();
    const [metrics, setMetrics] = useState([]);
    const [error, setError] = useState(false);
    Form.useWatch("metricValues", form);

    useEffect(() => {
        listMetrics(token, setMetrics, setError);
    }, [])

    const onFinish = async (values) => {
        //await createResource(values.resourceType, values.isSelfManaged, token, setNewResource, setError);
        console.log('Success', values);
        console.log(form.getFieldValue('metricValues')[0].metric);
    };
    const onFinishFailed = (errorInfo) => {
        console.log('Failed:', errorInfo);
    };

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
                            {fields.map(({key, name, ...field}) => (
                                <Space
                                    key={key}
                                    className="flex"
                                    align="baseline"
                                >
                                    <Form.Item
                                        {...field}
                                        name={[name, 'metric']}
                                    >
                                        <Select className="w-40" placeholder="Metric">
                                            {metrics.map(metric => {
                                                return (
                                                    <Select.Option value={metric.metric_id} key={metric.metric_id}>
                                                        {metric.metric}
                                                    </Select.Option>
                                                );
                                            })}

                                        </Select>
                                    </Form.Item>
                                    <Form.Item
                                        {...field}
                                        name={[name, 'value']}
                                        valuePropName="checked"
                                        rules={[
                                            {
                                                required: true,
                                                message: 'Missing value',
                                            },
                                        ]}
                                        className="w-40"
                                    >
                                        {metrics
                                            .find((metric) =>
                                                metric.metric_id === form
                                                    .getFieldValue('metricValues')[key]?.metric)
                                                    ?.metric_type.type === 'number' ?
                                            <InputNumber placeholder='0.00' controls={false} className="w-full"/> :
                                            metrics
                                                .find((metric) =>
                                                    metric.metric_id === form
                                                        .getFieldValue('metricValues')[key]?.metric)
                                                ?.metric_type.type === 'string' ?
                                                <Input placeholder='value' className="w-full"/> :
                                                <Checkbox defaultChecked={false} className="w-full">
                                                    Value
                                                </Checkbox>
                                        }

                                    </Form.Item>
                                    <MinusCircleOutlined onClick={() => remove(name)} />
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

                <Form.Item>
                    <Button type="primary" htmlType="submit">
                        Create
                    </Button>
                </Form.Item>
            </Form>
        </div>
    )
}

export default AddMetricValuesForm;