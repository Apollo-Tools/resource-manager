import DateFormatter from '../misc/DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import Link from 'next/link';
import {
  DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined, CloseCircleTwoTone, CheckCircleTwoTone, SyncOutlined,
} from '@ant-design/icons';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteEnsemble, listEnsembles, validateEnsemble} from '../../lib/EnsembleService';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';

const {Column} = Table;
const {confirm} = Modal;

const EnsembleTable = ({rowSelection}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [ensembles, setEnsembles] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listEnsembles(token, setEnsembles, setError);
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
      deleteEnsemble(id, token, setError)
          .then((result) => {
            if (result) {
              setEnsembles(ensembles.filter((ensemble) => ensemble.ensemble_id !== id));
            }
          });
    }
  };

  const onClickValidate = (id) => {
    if (!checkTokenExpired()) {
      validateEnsemble(id, token, setError)
          .then((result) => {
            setEnsembles((prevEnsembles) => {
              return prevEnsembles.map((ensemble) => {
                if (ensemble.ensemble_id === id) {
                  ensemble.is_valid = result;
                }
                return ensemble;
              });
            });
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this ensemble?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  const showValidateConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to validate this ensemble?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickValidate(id);
      },
    });
  };

  return (
    <Table
      dataSource={ensembles}
      rowKey={(record) => record.ensemble_id}
      rowSelection={rowSelection}
      className="h-[600px]"
      size='small'
    >
      <Column title="Id" dataIndex="ensemble_id" key="id"
        sorter={(a, b) => a.ensemble_id - b.ensemble_id}
        defaultSortOrder="ascend"
      />
      <Column title="Name" dataIndex="name" key="name"
        sorter={(a, b) =>
          a.name.localeCompare(b.name)}
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
        onFilter={(value, record) => record.name.startsWith(value)}
      />
      <Column title="Valid" dataIndex="is_valid" key="is_valid"
        render={(isValid) => {
          if (isValid) {
            return <CheckCircleTwoTone twoToneColor="#00ff00"/>;
          } else {
            return <CloseCircleTwoTone twoToneColor="#ff0000"/>;
          }
        }}
        sorter={(a, b) =>
          a.runtime.name.localeCompare(b.ensemble.is_valid)}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Updated at" dataIndex="updated_at" key="updated_at"
        render={(updatedAt) => <DateFormatter dateTimestamp={updatedAt}/>}
        sorter={(a, b) => a.updated_at - b.updated_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Link href={`/ensembles/${record.ensemble_id}`}>
              <Button icon={<InfoCircleOutlined />}/>
            </Link>
            <Button icon={<SyncOutlined />} onClick={() => showValidateConfirm(record.ensemble_id)}/>
            <Button onClick={() => showDeleteConfirm(record.function_id)} icon={<DeleteOutlined />}/>
          </Space>
        )}
      />
    </Table>
  );
};

EnsembleTable.propTypes = {
};

export default EnsembleTable;
