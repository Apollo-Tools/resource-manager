import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getService} from '../../lib/ServiceService';
import ServiceDetailsCard from '../../components/services/ServiceDetailsCard';

const FunctionDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [service, setService] = useState();
  const [error, setError] = useState(false);
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

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Service Details ({service?.service_id})</Typography.Title>
      <Divider />
      { service &&
        <ServiceDetailsCard service={service}/>
      }
    </div>
  );
};

export default FunctionDetails;
