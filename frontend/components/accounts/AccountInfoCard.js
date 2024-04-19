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
import TableSkeleton from '../misc/TableSkeleton';
import ContentSkeleton from '../misc/ContentSkeleton';

const AccountInfoCard = ({isAdmin, accountId, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [account, setAccount] = useState();
  const [accountNamespaces, setAccountNamespaces] = useState([]);
  const [isAccountLoading, setAccountLoading] = useState(true);
  const [isNamespacesLoading, setNamespacesLoading] = useState(true);
  const [isFinished, setFinished] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      if (isAdmin && accountId) {
        void getAccount(accountId, token, setAccount, setAccountLoading, setError);
        void listNamespaces(accountId, token, setAccountNamespaces, setNamespacesLoading, setError);
      } else {
        void getMyAccount(token, setAccount, setAccountLoading, setError);
        void listMyNamespaces(token, setAccountNamespaces, setNamespacesLoading, setError);
      }
    }
  }, []);

  useEffect(() => {
    if (isFinished) {
      if (isAdmin && accountId) {
        void listNamespaces(accountId, token, setAccountNamespaces, setNamespacesLoading, setError);
      } else {
        void listMyNamespaces(token, setAccountNamespaces, setNamespacesLoading, setError);
      }
      setFinished(false);
    }
  }, [isFinished]);

  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <Space className="col-span-6" direction="vertical" size="large">
        <TextDataDisplay label="Username" value={account?.username} isLoading={isAccountLoading}/>
        <TextDataDisplay label="Role" value={account?.role.role} isLoading={isAccountLoading}/>
        <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={account?.created_at} />} isLoading={isAccountLoading} />
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
            accountId={account?.account_id}
            hasActions={isAdmin}
            setError={setError}
            isLoading={isNamespacesLoading}
            setLoading={setNamespacesLoading}
          />
        </DataDisplay>
      </div>
      <div className="col-span-full" >
        {accountNamespaces &&
          <AddNamespaceForm
            accountId={account?.account_id}
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
