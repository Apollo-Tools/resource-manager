import NewCredentialsForm from './NewCredentialsForm';
import {Divider} from 'antd';
import CredentialsList from './CredentialsList';
import {useEffect, useState} from 'react';

const CredentialsCard = ({credentials, reloadCredentials, setCredentials}) => {
  const [isFinished, setFinished] = useState(false);

  useEffect(() => {
    if (isFinished) {
      reloadCredentials().then(() => setFinished(false));
    }
  }, [isFinished]);

  return (
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
  );
};

export default CredentialsCard;
