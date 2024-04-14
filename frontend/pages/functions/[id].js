import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import {getFunction} from '../../lib/api/FunctionService';
import UpdateFunctionFileForm from '../../components/functions/UpdateFunctionFileForm';
import UpdateFunctionSettingsForm from '../../components/functions/UpdateFunctionSettingsForm';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import PropTypes from 'prop-types';

const FunctionDetails = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [func, setFunction] = useState('');
  const [isLoading, setLoading] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      void getFunction(id, token, setFunction, setLoading, setError);
    }
  }, [id]);

  const reloadFunction = async () => {
    if (!checkTokenExpired()) {
      await getFunction(id, token, setFunction, setLoading, setError);
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
            <UpdateFunctionSettingsForm func={func} reloadFunction={reloadFunction} setError={setError}/>
            <Divider/>
            <UpdateFunctionFileForm func={func} reloadFunction={reloadFunction} setError={setError}/>
          </>
        }
      </div>
    </>
  );
};

FunctionDetails.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default FunctionDetails;
