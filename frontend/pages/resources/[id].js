import {getResource, listSubresources} from '../../lib/api/ResourceService';
import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Segmented, Tooltip, Typography} from 'antd';
import ResourceDetailsCard from '../../components/resources/ResourceDetailsCard';
import AddMetricValuesForm from '../../components/metrics/AddMetricValuesForm';
import {listResourceMetrics} from '../../lib/api/MetricValueService';
import MetricValuesTable from '../../components/metrics/MetricValuesTable';
import ResourceTable from '../../components/resources/ResourceTable';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import {listPlatformMetrics} from '../../lib/api/PlatformMetricService';
import {LockTwoTone, UnlockTwoTone} from '@ant-design/icons';
import {ICON_GREEN, ICON_RED} from '../../components/misc/Constants';

// TODO: add way to update values
const ResourceDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [resource, setResource] = useState();
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [metricValues, setMetricValues] = useState([]);
  const [mappedMetricValues, setMappedMetricValues] = useState([]);
  const [subresources, setSubresources] = useState([]);
  const [platformMetrics, setPlatformMetrics] = useState([]);
  const [isFinished, setFinished] = useState(false);
  const [error, setError] = useState(false);
  const [segments, setSegments] = useState([]);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getResource(id, token, setResource, setError);
      listSubresources(id, token, setSubresources, setError);
      listResourceMetrics(id, token, setMetricValues, setError);
    }
  }, [id]);

  useEffect(() => {
    if (!checkTokenExpired() && resource != null) {
      listPlatformMetrics(resource.platform.platform_id, token, setPlatformMetrics, setError);
    }
  }, [resource]);

  useEffect(() => {
    if (!checkTokenExpired() && subresources != null && resource != null) {
      setSelectedSegment('Details');
      setSegments(() => {
        if (subresources.length) {
          return ['Details', 'Subresources', 'Metric Values'];
        } else if (!resource.main_resource_id) {
          return ['Details', 'Metric Values'];
        } else {
          return ['Details'];
        }
      });
    }
  }, [subresources, resource]);

  useEffect(() => {
    if (metricValues != null) {
      mapValuesToValueField();
    }
  }, [metricValues]);

  useEffect(() => {
    setMappedMetricValues(metricValues.map((metricValue) => {
      const platformMetric = platformMetrics
          .find((metric) => metric.metric.metric_id === metricValue.metric.metric_id);
      metricValue.is_monitored = platformMetric?.is_monitored;
      return metricValue;
    }));
  }, [platformMetrics, metricValues]);

  useEffect(() => {
    if (isFinished) {
      listResourceMetrics(id, token, setMetricValues, setError);
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
    setMappedMetricValues(metricValues.map((metricValue) => {
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
    }));
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Resource Details`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>
          {resource?.name} ({resource?.resource_id})
          <Tooltip
            placement="top"
            title={`resource is ${resource?.is_locked ? 'locked' : 'not locked'}`}
            arrow={true}
            color={'#262626'}
            overlayClassName="w-fit max-w-sm text-center"
            overlayInnerStyle={{textAlign: 'center', padding: '6px'}}
            className="float-right"
          >
            {resource?.is_locked ?
              <LockTwoTone className="site-form-item-icon" twoToneColor={ICON_RED} /> :
              <UnlockTwoTone className="site-form-item-icon" twoToneColor={ICON_GREEN} />
            }
          </Tooltip>
        </Typography.Title>
        <Divider />
        <Segmented options={segments} value={selectedSegment}
          onChange={(e) => setSelectedSegment(e)} size="large" block/>
        <Divider />
        {
          selectedSegment === 'Details' && resource != null &&
          <ResourceDetailsCard resource={resource} setResource={setResource}/>
        }
        {selectedSegment === 'Metric Values' && resource != null && (
          <>
            <div>
              <MetricValuesTable
                resourceId={id}
                metricValues={mappedMetricValues}
                setMetricValues={setMappedMetricValues}
              />
            </div>
            <Divider />
            <AddMetricValuesForm
              resource={resource}
              excludeMetricIds={mappedMetricValues.map((metricValue) => metricValue.metric.metric_id)}
              setFinished={setFinished}
            />
          </>)
        }
        {
          selectedSegment === 'Subresources' && subresources != null && (
            <>
              <ResourceTable resources={subresources} resourceType='sub' hasActions/>
            </>
          )
        }
      </div>
    </>
  );
};

export default ResourceDetails;
