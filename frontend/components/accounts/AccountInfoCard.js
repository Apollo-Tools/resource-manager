import DateFormatter from '../misc/DateFormatter';
import ResetPasswordForm from './ResetPasswordForm';
import {Divider, Space} from 'antd';
import {useEffect, useState} from 'react';
import TextDataDisplay from '../misc/TextDataDisplay';
import DataDisplay from '../misc/DataDisplay';
import NamespaceTable from './NamespaceTable';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import AddNamespaceForm from './AddNamespaceForm';
import {getAccount, getMyAccount} from '../../lib/api/AccountService';
import {listMyNamespaces, listNamespaces} from '../../lib/api/AccountNamespaceService';
import PropTypes from 'prop-types';

const AccountInfoCard = ({isAdmin, accountId, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [account, setAccount] = useState();
  const [accountNamespaces, setAccountNamespaces] = useState([]);
  const [isLoading, setLoading] = useState(false);
  const [isFinished, setFinished] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      if (isAdmin && accountId) {
        void getAccount(accountId, token, setAccount, setLoading, setError);
        void listNamespaces(accountId, token, setAccountNamespaces, setLoading, setError);
      } else {
        void getMyAccount(token, setAccount, setLoading, setError);
        void listMyNamespaces(token, setAccountNamespaces, setLoading, setError);
      }
    }
  }, []);

  useEffect(() => {
    if (isFinished) {
      if (isAdmin && accountId) {
        void listNamespaces(accountId, token, setAccountNamespaces, setLoading, setError);
      } else {
        void listMyNamespaces(token, setAccountNamespaces, setLoading, setError);
      }
      setFinished(false);
    }
  }, [isFinished]);

  if (!account) {
    return;
  }
  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <Space className="col-span-6" direction="vertical" size="large">
        <TextDataDisplay label="Username" value={account.username} />
        <TextDataDisplay label="Role" value={account.role.role} />
        <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={account.created_at} />} />
      </Space>
      <div className="col-span-6">
        <DataDisplay label={'Reset Password'}>
          <ResetPasswordForm setError={setError}/>
        </DataDisplay>
      </div>
      <Divider className="col-span-full"/>
      <div className="col-span-full">
        <DataDisplay label="Namespaces" tooltip="Only one namespace per resource is allowed">
          <NamespaceTable
            namespaces={accountNamespaces}
            setFinished={setFinished}
            accountId={account.account_id}
            hasActions={isAdmin}
            setError={setError}
          />
        </DataDisplay>
      </div>
      <div className="col-span-full" >
        {accountNamespaces &&
          <AddNamespaceForm
            accountId={account.account_id}
            existingNamespaces={accountNamespaces}
            setFinished={setFinished}
            setError={setError}
          />
        }
      </div>
    </div>
  );
};

AccountInfoCard.propTypes = {
  isAdmin: PropTypes.bool.isRequired,
  accountId: PropTypes.number,
  setError: PropTypes.func.isRequired,
};

export default AccountInfoCard;
