import {Button, Modal, Space, Table, Tooltip} from 'antd';
import Link from 'next/link';
import {
  DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined, SyncOutlined,
} from '@ant-design/icons';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteEnsemble, listEnsembles, validateEnsemble} from '../../lib/api/EnsembleService';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';
import PropTypes from 'prop-types';
import BoolValueDisplay from '../misc/BoolValueDisplay';
import DateColumnRender from '../misc/DateColumnRender';

const {Column} = Table;
const {confirm} = Modal;

const EnsembleTable = ({rowSelection, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(false);
  const [ensembles, setEnsembles] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listEnsembles(token, setEnsembles, setLoading, setError);
    }
  }, []);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteEnsemble(id, token, setLoading, setError)
          .then((result) => {
            if (result) {
              setEnsembles(ensembles.filter((ensemble) => ensemble.ensemble_id !== id));
            }
          });
    }
  };

  const onClickValidate = (id) => {
    if (!checkTokenExpired()) {
      validateEnsemble(id, token, null, setLoading, setError)
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
        render={(isValid) => <BoolValueDisplay value={isValid}/>}
        sorter={(a, b) =>
          a.is_valid - b.is_valid}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={`/ensembles/${record.ensemble_id}`}>
                <Button icon={<InfoCircleOutlined />}/>
              </Link>
            </Tooltip>
            <Tooltip title="Validate">
              <Button icon={<SyncOutlined />} onClick={() => showValidateConfirm(record.ensemble_id)}/>
            </Tooltip>
            <Tooltip title="Delete">
              <Button onClick={() => showDeleteConfirm(record.ensemble_id)} icon={<DeleteOutlined />}/>
            </Tooltip>
          </Space>
        )}
      />
    </Table>
  );
};

EnsembleTable.propTypes = {
  rowSelection: PropTypes.object,
  setError: PropTypes.func.isRequired,
};

export default EnsembleTable;
