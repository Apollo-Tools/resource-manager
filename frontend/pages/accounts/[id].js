import {useRouter} from 'next/router';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Typography} from 'antd';
import AccountInfoCard from '../../components/accounts/AccountInfoCard';
import {siteTitle} from '../../components/misc/Sidebar';
import Head from 'next/head';
import PropTypes from 'prop-types';

const AccountDetails = ({setError}) => {
  const {payload} = useAuth();
  const router = useRouter();
  const {id} = router.query;

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Account Details`}</title>
      </Head>
      <div className="default-card">
        <Typography.Title level={2}>Account Details ({id})</Typography.Title>
        <Divider />
        { id &&
          <AccountInfoCard
            accountId={Number(id)}
            isAdmin={payload?.role?.[0] === 'admin'}
            setError={setError}
          />
        }
      </div>
    </>
  );
};

AccountDetails.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default AccountDetails;
