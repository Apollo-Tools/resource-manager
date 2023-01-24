import DateFormatter from './DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import Link from 'next/link';
import {DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined} from '@ant-design/icons';
import {useAuth} from '../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteFunction, listFunctions} from '../lib/FunctionService';

const {Column} = Table;
const {confirm} = Modal;

const FunctionTable = () => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [functions, setFunctions] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listFunctions(token, setFunctions, setError);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteFunction(id, token, setError)
          .then((result) => {
            if (result) {
              setFunctions(functions.filter((func) => func.function_id !== id));
            }
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this function?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  return (
    <Table dataSource={functions} rowKey={(record) => record.function_id}>
      <Column title="Id" dataIndex="function_id" key="id"
        sorter={(a, b) => a.function_id - b.function_id}
        defaultSortOrder="ascend"
      />
      <Column title="Name" dataIndex="name" key="name"
        sorter={(a, b) =>
          a.name.localeCompare(b.name)}
      />
      <Column title="Runtime" dataIndex="runtime" key="runtime"
        render={(runtime) => runtime.name}
        sorter={(a, b) =>
          a.runtime.name.localeCompare(b.runtime.name)}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Link href={`/functions/${record.function_id}`}>
              <Button icon={<InfoCircleOutlined />}/>
            </Link>
            <Button onClick={() => showDeleteConfirm(record.function_id)} icon={<DeleteOutlined />}/>
          </Space>
        )}
      />
    </Table>
  );
};

export default FunctionTable;
