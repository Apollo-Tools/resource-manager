import DateFormatter from '../misc/DateFormatter';
import ResetPasswordForm from './ResetPasswordForm';
import {Space} from 'antd';
import {useEffect, useState} from 'react';
import {getAccount} from '../../lib/AccountService';
import {useAuth} from '../../lib/AuthenticationProvider';
import TextDataDisplay from '../misc/TextDataDisplay';
import DataDisplay from '../misc/DataDisplay';

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
      <Space className="basis-full md:basis-1/2" direction="vertical" size="large">
        <TextDataDisplay label="Username" value={account.username} />
        <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={account.created_at} />} />
      </Space>
      <div className="basis-full md:basis-1/2">
        <DataDisplay label={'Reset Password'}>
          <ResetPasswordForm />
        </DataDisplay>
      </div>
    </div>
  );
};

export default AccountInfoCard;
