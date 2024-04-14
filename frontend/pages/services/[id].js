import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getService} from '../../lib/api/ServiceService';
import NewUpdateServiceForm from '../../components/services/NewUpdateServiceForm';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import PropTypes from 'prop-types';

const ServiceDetails = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [service, setService] = useState();
  const [isFinished, setFinished] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      void getService(id, token, setService, setLoading, setError);
    }
  }, [id]);

  useEffect(() => {
    if (isFinished) {
      setFinished(false);
      void getService(id, token, setService, setLoading, setError);
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
          <NewUpdateServiceForm
            setNewService={setService}
            service={service}
            mode='update'
            setFinished={setFinished}
            setError={setError}
          />
        }
      </div>
    </>
  );
};

ServiceDetails.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default ServiceDetails;
