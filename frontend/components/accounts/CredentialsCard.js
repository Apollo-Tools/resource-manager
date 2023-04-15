import NewCredentialsForm from './NewCredentialsForm';
import {Divider, Typography} from 'antd';
import CredentialsList from './CredentialsList';
import {useEffect, useState} from 'react';
import {listCredentials} from '../../lib/CredentialsService';
import {useAuth} from '../../lib/AuthenticationProvider';

const CredentialsCard = () => {
  const {token, checkTokenExpired} = useAuth();
  const [isFinished, setFinished] = useState(false);
  const [credentials, setCredentials] = useState();
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      reloadCredentials();
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (isFinished) {
      reloadCredentials().then(() => setFinished(false));
    }
  }, [isFinished]);

  const reloadCredentials = async () => {
    return listCredentials(token, setCredentials, setError);
  };
  if (!credentials) {
    return <></>;
  }

  return (
    <>
      <Typography.Title level={2}>Credentials</Typography.Title>
      <div className="flex md:flex-row flex-col">
        <div className="basis-full md:basis-1/2 md:pr-8">
          <NewCredentialsForm excludeProviders={credentials?.map((credentials) =>
            credentials.resource_provider.provider_id)} setFinished={setFinished}/>
        </div>
        <Divider className="md:hidden"/>
        <div className="basis-full md:basis-1/2 md:pl-8">
          <CredentialsList credentials={credentials} setCredentials={setCredentials}/>
        </div>
      </div>
    </>
  );
};

export default CredentialsCard;
