import {Button, Modal, Table, Tooltip} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';
import {deleteFunctionType, listFunctionTypes} from '../../lib/FunctionTypeService';
import PropTypes from 'prop-types';
import {deleteServiceType, listServiceTypes} from '../../lib/ServiceTypeService';
import DateColumnRender from "../misc/DateColumnRender";

const {Column} = Table;
const {confirm} = Modal;

const ArtifactTypeTable = ({artifact}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [artifactTypes, setArtifactTypes] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      if (artifact === 'service') {
        listServiceTypes(token, setArtifactTypes, setError);
      } else {
        listFunctionTypes(token, setArtifactTypes, setError);
      }
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
      let deleteArtifactType;
      if (artifact === 'service') {
        deleteArtifactType = deleteServiceType(id, token, setError);
      } else {
        deleteArtifactType = deleteFunctionType(id, token, setError);
      }
      deleteArtifactType.then((result) => {
        if (result) {
          setArtifactTypes(artifactTypes.filter((types) => types.artifact_type_id !== id));
        }
      });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this function type and all related functions?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  return (
    <Table
      dataSource={artifactTypes}
      rowKey={(record) => record.artifact_type_id}
      size="small"
    >
      <Column title="Id" dataIndex="artifact_type_id" key="id"
        sorter={(a, b) => a.artifact_type_id - b.artifact_type_id}
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
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Tooltip title="Delete">
            <Button onClick={() => showDeleteConfirm(record.artifact_type_id)} icon={<DeleteOutlined />}/>
          </Tooltip>
        )}
      />
    </Table>
  );
};

ArtifactTypeTable.propTypes = {
  artifact: PropTypes.oneOf(['service', 'function']).isRequired,
};

export default ArtifactTypeTable;
