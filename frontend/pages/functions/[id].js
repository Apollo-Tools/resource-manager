import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getFunction} from '../../lib/FunctionService';
import UpdateFunctionForm from '../../components/functions/UpdateFunctionForm';

const FunctionDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [func, setFunction] = useState('');
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getFunction(id, token, setFunction, setError);
    }
  }, [id]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const reloadFunction = async () => {
    if (!checkTokenExpired()) {
      await getFunction(id, token, setFunction, setError);
    }
  };

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Function Details ({func.function_id})</Typography.Title>
      <Divider />
      {
        func &&
        <UpdateFunctionForm func={func} reloadFunction={reloadFunction}/>
      }
    </div>
  );
};

export default FunctionDetails;
