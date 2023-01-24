import DateFormatter from './DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {deleteCredentials} from '../lib/CredentialsService';
import {useAuth} from '../lib/AuthenticationProvider';
import PropTypes from 'prop-types';

const {Column} = Table;
const {confirm} = Modal;

const CredentialsList = ({credentials, setCredentials}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteCredentials(id, token, setError)
          .then((result) => {
            if (result) {
              setCredentials(credentials.filter((credential) => credential.credentials_id !== id));
            }
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete these credentials?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  return (
    <>
      <Table dataSource={credentials} rowKey={(record) => record.credentials_id} size="small">
        <Column title="Provider" dataIndex="resource_provider" key="resource_provider"
          render={(provider) => provider.provider}
          sorter={(a, b) => a.provider - b.provider}
          defaultSortOrder="ascend"
        />
        <Column title="Created at" dataIndex="created_at" key="created_at"
          render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
          sorter={(a, b) => a.created_at - b.created_at}
        />
        <Column title="Actions" key="action"
          render={(_, credential) => (
            <Space size="middle">
              <Button onClick={() => showDeleteConfirm(credential.credentials_id)} icon={<DeleteOutlined />}/>
            </Space>
          )}
        />
      </Table>
    </>
  );
};

CredentialsList.propTypes = {
  credentials: PropTypes.arrayOf(PropTypes.object),
  setCredentials: PropTypes.func,
};

export default CredentialsList;
