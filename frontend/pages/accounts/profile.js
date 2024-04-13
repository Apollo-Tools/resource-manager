import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import {useState} from 'react';
import {Divider, Segmented, Typography} from 'antd';
import AccountInfoCard from '../../components/accounts/AccountInfoCard';
import CredentialsCard from '../../components/accounts/CredentialsCard';
import VPCCard from '../../components/accounts/VPCCard';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
const {Title} = Typography;

const Profile = ({setError}) => {
  const {payload} = useAuth();
  const [selectedSegment, setSelectedSegment] = useState('Account Info');

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
          <AccountInfoCard isAdmin={payload?.role?.[0] === 'admin'} setError={setError}/> :
          selectedSegment === 'Cloud Credentials' ?
          <CredentialsCard setError={setError}/> :
          <VPCCard setError={setError}/>
        }
      </div>
    </>
  );
};

Profile.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default Profile;
