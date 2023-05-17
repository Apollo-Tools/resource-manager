import {getResource} from '../../lib/ResourceService';
import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Segmented, Typography} from 'antd';
import ResourceDetailsCard from '../../components/resources/ResourceDetailsCard';
import AddMetricValuesForm from '../../components/metrics/AddMetricValuesForm';
import {listResourceMetrics} from '../../lib/MetricValueService';
import MetricValuesTable from '../../components/metrics/MetricValuesTable';

// TODO: add way to update values
const ResourceDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [resource, setResource] = useState();
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [metricValues, setMetricValues] = useState([]);
  const [isFinished, setFinished] = useState(false);
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getResource(id, token, setResource, setError);
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
  }, [isFinished]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const mapValuesToValueField = () => {
    setMetricValues((prevValues) => {
      return prevValues.map((metricValue) => {
        let value = '';
        switch (metricValue.metric.metric_type.type) {
          case 'number':
            value = metricValue.value_number;
            break;
          case 'string':
            value = metricValue.value_string;
            break;
          case 'boolean':
            value = metricValue.value_bool.toString();
            break;
        }
        metricValue.value = value;
        return metricValue;
      });
    });
  };

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Resource Details ({resource?.resource_id})</Typography.Title>
      <Divider />
      <Segmented options={['Details', 'Metric Values']} value={selectedSegment}
        onChange={(e) => setSelectedSegment(e)} size="large" block/>
      <Divider />
      {
        selectedSegment === 'Details' && resource != null &&
          <ResourceDetailsCard resource={resource}/>
      }
      {
        selectedSegment === 'Metric Values' && resource != null && (
          <>
            <div>
              <MetricValuesTable resourceId={id} metricValues={metricValues} setMetricValues={setMetricValues}/>
            </div>
            <Divider />
            <AddMetricValuesForm
              resource={resource}
              excludeMetricIds={metricValues.map((metricValue) => metricValue.metric.metric_id)}
              setFinished={setFinished}
            />
          </>)
      }
    </div>
  );
};

export default ResourceDetails;
