import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getFunction} from '../../lib/api/FunctionService';
import UpdateFunctionFileForm from '../../components/functions/UpdateFunctionFileForm';
import UpdateFunctionSettingsForm from '../../components/functions/UpdateFunctionSettingsForm';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';

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
    <>
      <Head>
        <title>{`${siteTitle}: Function Details`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>Function Details ({func.function_id})</Typography.Title>
        <Divider />
        {
          func && <>
            <UpdateFunctionSettingsForm func={func} reloadFunction={reloadFunction}/>
            <Divider/>
            <UpdateFunctionFileForm func={func} reloadFunction={reloadFunction}/>
          </>
        }
      </div>
    </>
  );
};

export default FunctionDetails;
