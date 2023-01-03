import {siteTitle} from '../../components/Sidebar';
import Head from 'next/head';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {getAccount} from '../../lib/AccountService';
import DateFormatter from '../../components/DateFormatter';
import {Divider, Typography} from 'antd';
import ResetPasswordForm from '../../components/ResetPasswordForm';
const {Title} = Typography;

const Profile = () => {
  const {token, checkTokenExpired} = useAuth();
  const [account, setAccount] = useState();
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      getAccount(token, setAccount, setError);
    }
  }, []);

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Profile`}</title>
      </Head>
      <div className="card container w-11/12 max-w-7xl p-10">
        <Title>Profile</Title>
        <Divider />
        {account && <>
          <div className="flex md:flex-row flex-col">
            <div className="basis-full md:basis-1/2">
              <Title level={4}>Username</Title>
              {account.username}
              <Title level={4}>Created At</Title>
              <DateFormatter dateString={account.created_at} />
            </div>
            <div className="basis-full md:basis-1/2">
              <Title level={4}>Reset Password</Title>
              <ResetPasswordForm />
            </div>
          </div>
        </>
        }
        <Divider />
        <div>

        </div>
      </div>
    </>
  );
};

export default Profile;
