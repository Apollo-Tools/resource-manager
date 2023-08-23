import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import AccountInfoCard from '../../components/accounts/AccountInfoCard';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';

const AccountDetails = () => {
  const {payload} = useAuth();
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

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
        <title>{`${siteTitle}: Account Details`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>Account Details ({id})</Typography.Title>
        <Divider />
        { id &&
          <AccountInfoCard accountId={Number(id)} isAdmin={payload?.role?.[0] === 'admin'}/>
        }
      </div>
    </>
  );
};

export default AccountDetails;
