import DateFormatter from '../misc/DateFormatter';
import {Table, Tag} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';
import {listAccounts} from '../../lib/AccountService';
import {CheckCircleTwoTone, CloseCircleTwoTone} from '@ant-design/icons';

const {Column} = Table;

const AccountTable = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [accounts, setAccounts] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listAccounts(token, setAccounts, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  return (
    <Table
      dataSource={accounts}
      rowKey={(record) => record.account_id}
      size="small"
    >
      <Column title="Id" dataIndex="account_id" key="id"
        sorter={(a, b) => a.account_id - b.account_id}
        defaultSortOrder="ascend"
      />
      <Column title="Name" dataIndex="username" key="name"
        sorter={(a, b) =>
          a.username.localeCompare(b.username)}
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
        onFilter={(value, record) => record.username.startsWith(value)}
      />
      <Column title="Role" dataIndex="role" key="role"
        sorter={(a, b) =>
          a.role.role.localeCompare(b.role.role)}
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="role" />}
        onFilter={(value, record) => record.role.role.startsWith(value)}
        render={(role) => {
          let color = 'cyan';
          if (role.role === 'admin') {
            color = 'gold';
          }
          return <Tag color={color}>{role.role}</Tag>;
        }}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Is active" dataIndex={'is_active'} key="is_active"
        render={(isActive) => {
          if (isActive) {
            return <CheckCircleTwoTone twoToneColor="#00ff00"/>;
          } else {
            return <CloseCircleTwoTone twoToneColor="#ff0000"/>;
          }
        }}
      />
    </Table>
  );
};

AccountTable.propTypes = {
};

export default AccountTable;
