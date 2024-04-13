import {Button, Modal, Table, Tooltip} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useState} from 'react';
import Link from 'next/link';
import {deleteNamespaceFromAccount} from '../../lib/api/AccountNamespaceService';
import PropTypes from 'prop-types';
import DateColumnRender from '../misc/DateColumnRender';
const {Column} = Table;
const {confirm} = Modal;

const NamespaceTable = ({namespaces, setFinished, accountId, hasActions, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState();

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to remove this namespace from the account?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  const onClickDelete = (namespaceId) => {
    if (!checkTokenExpired()) {
      deleteNamespaceFromAccount(accountId, namespaceId, token, setLoading, setError)
          .then((result) => {
            if (result) {
              setFinished(true);
            }
          });
    }
  };

  return (
    <Table
      dataSource={namespaces}
      rowKey={(namespace) => namespace.namespace_id}
      size="small"
    >
      <Column title="Id" dataIndex="namespace_id" key="id"
        sorter={(a, b) => a.namespace_id - b.namespace_id}
        defaultSortOrder="ascend"
      />
      <Column title="Namespace" dataIndex="namespace" key="namespace"
        sorter={(a, b) =>
          a.namespace.localeCompare(b.namespace)}
      />
      <Column title="Resource" dataIndex="resource" key="resource"
        render={(resource) =>
          <Link href={`/resources/${resource.resource_id}`}>
            <Button type="link" size="small">{resource.name}</Button>
          </Link>}
        sorter={ (a, b) => a.resource.name.localeCompare(b.resource.name) }
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      {hasActions && <Column title="Actions" key="action"
        render={(_, namespace) => (
          <Tooltip title="Delete">
            <Button onClick={() => showDeleteConfirm(namespace.namespace_id)}
              icon={<DeleteOutlined />}/>
          </Tooltip>
        )}
      />}
    </Table>
  );
};

NamespaceTable.propTypes = {
  namespaces: PropTypes.arrayOf(PropTypes.object).isRequired,
  setFinished: PropTypes.func,
  accountId: PropTypes.number.isRequired,
  hasActions: PropTypes.bool.isRequired,
  setError: PropTypes.func.isRequired,
};

export default NamespaceTable;
