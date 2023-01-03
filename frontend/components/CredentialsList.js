import DateFormatter from './DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useState} from 'react';
import {deleteCredentials} from '../lib/CredentialsService';
import {useAuth} from '../lib/AuthenticationProvider';

const {Column} = Table;
const {confirm} = Modal;

const CredentialsList = ({credentials, setCredentials}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);

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
          render={(createdAt) => <DateFormatter dateString={createdAt}/>}
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

export default CredentialsList;
