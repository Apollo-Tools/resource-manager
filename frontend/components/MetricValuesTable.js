import DateFormatter from './DateFormatter';
import {Button, Modal, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {deleteResourceMetric} from '../lib/MetricValueService';
import {useAuth} from '../lib/AuthenticationProvider';
import {useState} from 'react';

const {Column} = Table;
const {confirm} = Modal;

const MetricValuesTable = ({resourceId, metricValues, setMetricValues}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();

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

  const onClickDelete = (metricId) => {
    if (!checkTokenExpired()) {
      deleteResourceMetric(resourceId, metricId, token, setError)
          .then((result) => {
            if (result) {
              setMetricValues(metricValues.filter((metricValue) => metricValue.metric.metric_id !== metricId));
            }
          });
    }
  };

  return (
    <Table dataSource={metricValues} rowKey={(mv) => mv.metric_id}>
      <Column title="Metric" dataIndex={['metric', 'metric']} key="metric"
        sorter={(a, b) =>
          a.metric.metric.localeCompare(b.metric.metric)}
      />
      <Column title="Is monitored" dataIndex={['metric', 'is_monitored']} key="is_monitored"
        render={(isMonitored) => isMonitored.toString()}
      />
      <Column title="Value" dataIndex="value" key="is_monitored" />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateString={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Modified at" dataIndex="updated_at" key="updated_at"
        render={(updatedAt) => <DateFormatter dateString={updatedAt}/>}
        sorter={(a, b) => a.updated_at - b.updated_at}
      />
      <Column title="Actions" key="action"
        render={(_, metricValue) => (
          <Button onClick={() => showDeleteConfirm(metricValue.metric.metric_id)}
            icon={<DeleteOutlined />}/>
        )}
      />
    </Table>
  );
};

export default MetricValuesTable;
