import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {useEffect, useState} from 'react';
import {Divider, Segmented, Typography} from 'antd';
import AccountInfoCard from '../../components/accounts/AccountInfoCard';
import CredentialsCard from '../../components/accounts/CredentialsCard';
import VPCCard from '../../components/accounts/VPCCard';
import {useAuth} from '../../lib/AuthenticationProvider';
const {Title} = Typography;

const Profile = () => {
  const {payload} = useAuth();
  const [selectedSegment, setSelectedSegment] = useState('Account Info');
  const [error, setError] = useState(false);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);


  return (
    <>
      <Head>
        <title>{`${siteTitle}: Profile`}</title>
      </Head>
      <div className="default-card">
        <Title>Profile</Title>
        <Divider />
        <Segmented options={['Account Info', 'Cloud Credentials', 'Virtual Private Clouds']} value={selectedSegment}
          onChange={(e) => setSelectedSegment(e)} size="large" block={true}/>
        <Divider />
        { selectedSegment === 'Account Info' ?
          <AccountInfoCard isAdmin={payload?.role?.[0] === 'admin'}/> :
          selectedSegment === 'Cloud Credentials' ?
          <CredentialsCard /> :
          <VPCCard />
        }
      </div>
    </>
  );
};

export default Profile;
