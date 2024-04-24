import {Button, Empty, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {deleteCredentials} from '../../lib/api/CredentialsService';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import DateColumnRender from '../misc/DateColumnRender';
import TableSkeleton from '../misc/TableSkeleton';

const {Column} = Table;
const {confirm} = Modal;

const CredentialsTable = ({credentials, setCredentials, isLoading, setLoading, setError}) => {
  const {token, checkTokenExpired} = useAuth();

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteCredentials(id, token, setLoading, setError)
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
      <Table
        dataSource={credentials}
        rowKey={(record) => record.credentials_id}
        size="small"
        locale={{emptyText: isLoading ? <TableSkeleton /> : <Empty />}}
      >
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

CredentialsTable.propTypes = {
  credentials: PropTypes.arrayOf(PropTypes.object).isRequired,
  setCredentials: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  setLoading: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default CredentialsTable;
