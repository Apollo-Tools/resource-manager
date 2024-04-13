import {Button, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useState} from 'react';
import {deleteCredentials} from '../../lib/api/CredentialsService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import DateColumnRender from '../misc/DateColumnRender';

const {Column} = Table;
const {confirm} = Modal;

const CredentialsList = ({credentials, setCredentials, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteCredentials(id, token, setLoading, setError)
          .then((result) => {
            if (result) {
              setCredentials(credentials.filter((credential) => credential.credentials_id !== id));
            } else {
              setError(new Error('failed to delete entry'));
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
          render={(createdAt) => <DateColumnRender value={createdAt}/>}
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
  credentials: PropTypes.arrayOf(PropTypes.object).isRequired,
  setCredentials: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default CredentialsList;
