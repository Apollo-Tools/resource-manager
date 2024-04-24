import NewCredentialsForm from './NewCredentialsForm';
import {Divider, Typography} from 'antd';
import CredentialsTable from './CredentialsTable';
import {useEffect, useState} from 'react';
import {listCredentials} from '../../lib/api/CredentialsService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';

const CredentialsCard = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isFinished, setFinished] = useState(false);
  const [credentials, setCredentials] = useState([]);
  const [isLoading, setLoading] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void reloadCredentials();
    }
  }, []);

  useEffect(() => {
    if (!checkTokenExpired() && isFinished) {
      reloadCredentials().then(() => setFinished(false));
    } else {
      setFinished(false);
    }
  }, [isFinished]);

  const reloadCredentials = async () => {
    return listCredentials(token, setCredentials, setLoading, setError);
  };

  return (
    <>
      <Typography.Title level={2}>Credentials</Typography.Title>
      <div className="flex md:flex-row flex-col">
        <div className="basis-full md:basis-1/2 md:pr-8">
          <NewCredentialsForm
            excludeProviders={credentials?.map((credentials) => credentials.resource_provider.provider_id)}
            setFinished={setFinished}
            isLoading={isLoading}
            setError={setError}
          />
        </div>
        <Divider className="md:hidden"/>
        <div className="basis-full md:basis-1/2 md:pl-8">
          <CredentialsTable
            credentials={credentials}
            setCredentials={setCredentials}
            isLoading={isLoading}
            setLoading={setLoading}
            setError={setError}
          />
        </div>
      </div>
    </>
  );
};

CredentialsCard.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default CredentialsCard;
