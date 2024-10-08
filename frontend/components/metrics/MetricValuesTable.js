import {Button, Empty, Modal, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {deleteResourceMetric} from '../../lib/api/MetricValueService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useState} from 'react';
import PropTypes from 'prop-types';
import TooltipIcon from '../misc/TooltipIcon';
import DateColumnRender from '../misc/DateColumnRender';
import TableSkeleton from '../misc/TableSkeleton';

const {Column} = Table;
const {confirm} = Modal;

const MetricValuesTable = ({resourceId, metricValues, setMetricValues, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState();

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
      deleteResourceMetric(resourceId, metricId, token, setLoading, setError)
          .then((result) => {
            if (result) {
              setMetricValues(metricValues.filter((metricValue) => metricValue.metric.metric_id !== metricId));
            }
          });
    }
  };

  return (
    <Table
      dataSource={metricValues}
      rowKey={(mv) => mv.metric_value_id}
      locale={{emptyText: isLoading ? <TableSkeleton /> : <Empty />}}
    >
      <Column title="Metric" dataIndex={['metric', 'metric']} key="metric"
        sorter={(a, b) =>
          a.metric.metric.localeCompare(b.metric.metric)}
        render={(_, metric) => {
          return <>{metric.metric.metric} <TooltipIcon text={metric.metric.description} /></>;
        }}
        defaultSortOrder="ascend"
      />
      <Column title="Value" dataIndex="value" key="value" />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Modified at" dataIndex="updated_at" key="updated_at"
        render={(updatedAt) => <DateColumnRender value={updatedAt}/>}
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

MetricValuesTable.propTypes = {
  resourceId: PropTypes.oneOfType([PropTypes.number, PropTypes.string]),
  metricValues: PropTypes.arrayOf(PropTypes.object),
  setMetricValues: PropTypes.func,
  setError: PropTypes.func.isRequired,
};

export default MetricValuesTable;
