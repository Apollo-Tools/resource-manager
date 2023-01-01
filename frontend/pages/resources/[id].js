import { getResource } from '../../lib/ResourceService';
import { useRouter } from 'next/router'
import { useEffect, useState } from 'react';
import { useAuth } from '../../lib/AuthenticationProvider';
import { Divider, Segmented, Table, Modal, Button } from 'antd';
import { listResourceTypes } from '../../lib/ResourceTypeService';
import UpdateResourceForm from '../../components/UpdateResourceForm';
import AddMetricValuesForm from '../../components/AddMetricValuesForm';
import { deleteResourceMetric, listResourceMetrics } from '../../lib/MetricValueService';
import Date from '../../components/Date';
import { DeleteOutlined, ExclamationCircleFilled } from '@ant-design/icons';

const { Column } = Table;
const { confirm } = Modal;

// TODO: add way to update values
const ResourceDetails = () => {
    const {token, checkTokenExpired} = useAuth();
    const [resource, setResource] = useState('');
    const [selectedSegment, setSelectedSegment] = useState('Details')
    const [resourceTypes, setResourceTypes] = useState([])
    const [metricValues, setMetricValues] = useState([]);
    const [isFinished, setFinished] = useState(false);
    const [error, setError] = useState(false)
    const router = useRouter();
    const { id } = router.query;

    useEffect(() => {
        if (!checkTokenExpired() && id != null) {
            getResource(id, token,  setResource, setError);
            listResourceTypes(token, setResourceTypes, setError);
            listResourceMetrics(id, token, setMetricValues, setError)
                .then(mapValuesToValueField);
        }
    }, [id]);

    useEffect(() => {
        if (isFinished) {
            listResourceMetrics(id, token, setMetricValues, setError)
                .then(mapValuesToValueField);
            setFinished(false);
        }
    }, [isFinished])

    const reloadResource = async () => {
        await getResource(id, token,  setResource, setError);
    }

    const onClickDelete = (metricId) => {
        if (!checkTokenExpired()) {
            deleteResourceMetric(id, metricId, token, setError)
                .then(result => {
                    if (result) {
                        setMetricValues(metricValues.filter(metricValue => metricValue.metric.metric_id !== metricId));
                    }
                });
        }
    }

    const showDeleteConfirm = (id) => {
        confirm({
            title: 'Confirmation',
            icon: <ExclamationCircleFilled />,
            content: 'Are you sure you want to delete this metric value?',
            okText: 'Yes',
            okType: 'danger',
            cancelText: 'No',
            onOk() {
                onClickDelete(id);
            },
        });
    };

    const mapValuesToValueField = () => {
        setMetricValues(prevValues => {
            return prevValues.map(metricValue => {
                let value = "";
                switch (metricValue.metric.metric_type.type) {
                    case "number":
                        value = metricValue.value_number;
                        break;
                    case "string":
                        value = metricValue.value_string;
                        break;
                    case "boolean":
                        value = metricValue.value_bool.toString();
                        break;
                }
                metricValue.value = value;
                return metricValue;
            });
        });
    }

    return (
        <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
            <h2>Resource Details ({resource.resource_id})</h2>
            <Divider />
            <Segmented options={['Details', 'Metric Values']} value={selectedSegment}
                       onChange={(e) => setSelectedSegment(e)} size="large"/>
            <Divider />
            {
                selectedSegment === 'Details' &&
                <UpdateResourceForm resource={resource} resourceTypes={resourceTypes} reloadResource={reloadResource}/>
            }
            {
                selectedSegment === 'Metric Values' && (
                    <>
                        <div>
                            <Table dataSource={metricValues} rowKey={(mv) => mv.metric_id}>
                                <Column title="Metric" dataIndex={["metric", "metric"]} key="metric"
                                        sorter={(a, b) =>
                                            a.metric.localeCompare(b.metric)}
                                />
                                <Column title="Is monitored" dataIndex={["metric", "is_monitored"]} key="is_monitored"
                                        render={(isMonitored) => isMonitored.toString()}
                                />
                                <Column title="Value" dataIndex="value" key="is_monitored" />
                                <Column title="Created at" dataIndex="created_at" key="created_at"
                                        render={(createdAt) => <Date dateString={createdAt}/>}
                                        sorter={(a, b) => a.created_at - b.created_at}
                                />
                                <Column title="Modified at" dataIndex="updated_at" key="updated_at"
                                        render={(updatedAt) => <Date dateString={updatedAt}/>}
                                        sorter={(a, b) => a.updated_at - b.updated_at}
                                />
                                <Column title="Actions" key="action"
                                        render={(_, metricValue) => (
                                            <Button onClick={() => showDeleteConfirm(metricValue.metric.metric_id)}
                                                    icon={<DeleteOutlined />}/>
                                        )}
                                />
                            </Table>
                        </div>
                        <Divider />
                        <AddMetricValuesForm
                            resource={resource}
                            excludeMetricIds={metricValues.map(metricValue => metricValue.metric.metric_id)}
                            setFinished={setFinished}
                        />
                    </>)
            }
        </div>
    );
}

export default ResourceDetails;