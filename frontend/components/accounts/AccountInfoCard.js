import DateFormatter from '../misc/DateFormatter';
import ResetPasswordForm from './ResetPasswordForm';
import {Typography} from 'antd';
import {useEffect, useState} from 'react';
import {getAccount} from '../../lib/AccountService';
import {useAuth} from '../../lib/AuthenticationProvider';

const {Title} = Typography;

const AccountInfoCard = () => {
  const {token, checkTokenExpired} = useAuth();
  const [account, setAccount] = useState();
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      getAccount(token, setAccount, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  if (!account) {
    return;
  }
  return (
    <div className="flex md:flex-row flex-col">
      <div className="basis-full md:basis-1/2">
        <Title level={5} className="mt-0.5">Username</Title>
        {account.username}
        <Title level={5}>Created At</Title>
        <DateFormatter dateTimestamp={account.created_at} />
      </div>
      <div className="basis-full md:basis-1/2">
        <Title level={5} className="mt-0.5">Reset Password</Title>
        <ResetPasswordForm />
      </div>
    </div>
  );
};

export default AccountInfoCard;
