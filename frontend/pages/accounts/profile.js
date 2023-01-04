import {siteTitle} from '../../components/Sidebar';
import Head from 'next/head';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {getAccount} from '../../lib/AccountService';
import {Divider, Segmented, Typography} from 'antd';
import AccountInfoCard from '../../components/AccountInfoCard';
import {listCredentials} from '../../lib/CredentialsService';
import CredentialsCard from '../../components/CredentialsCard';
const {Title} = Typography;

const Profile = () => {
  const {token, checkTokenExpired} = useAuth();
  const [account, setAccount] = useState();
  const [credentials, setCredentials] = useState();
  const [selectedSegment, setSelectedSegment] = useState('Account Info');
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      getAccount(token, setAccount, setError);
      reloadCredentials();
    }
  }, []);

  const reloadCredentials = async () => {
    return listCredentials(token, setCredentials, setError);
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Profile`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Title>Profile</Title>
        <Divider />
        <Segmented options={['Account Info', 'Cloud Credentials']} value={selectedSegment}
          onChange={(e) => setSelectedSegment(e)} size="large" block={true}/>
        <Divider />
        { selectedSegment === 'Account Info' ?
          <AccountInfoCard account={account}/> :
          <CredentialsCard
            credentials={credentials}
            reloadCredentials={reloadCredentials}
            setCredentials={setCredentials}
          />
        }
      </div>
    </>
  );
};

export default Profile;
