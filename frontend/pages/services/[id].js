import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getService} from '../../lib/ServiceService';
import NewUpdateServiceForm from '../../components/services/NewUpdateServiceForm';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';

const FunctionDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [service, setService] = useState();
  const [error, setError] = useState(false);
  const [isFinished, setFinished] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getService(id, token, setService, setError);
    }
  }, [id]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (isFinished) {
      setFinished(false);
      getService(id, token, setService, setError);
    }
  }, [isFinished]);

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Service Details`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>Service Details ({service?.service_id})</Typography.Title>
        <Divider />
        { service &&
          <NewUpdateServiceForm setNewService={setService} service={service} mode='update' setFinished={setFinished}/>
        }
      </div>
    </>
  );
};

export default FunctionDetails;
