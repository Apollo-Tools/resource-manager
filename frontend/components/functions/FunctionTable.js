import DateFormatter from '../misc/DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import Link from 'next/link';
import {DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined} from '@ant-design/icons';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteFunction, listFunctions} from '../../lib/FunctionService';
import ResourceTable from '../resources/ResourceTable';
import PropTypes from 'prop-types';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';

const {Column} = Table;
const {confirm} = Modal;

const FunctionTable = ({value = {}, onChange, hideDelete, isExpandable, resources}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [functions, setFunctions] = useState([]);
  const [selectedResources, setSelectedResources] = useState(new Map());

  useEffect(() => {
    if (!checkTokenExpired()) {
      listFunctions(token, setFunctions, setError);
      setSelectedResources(value);
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    onChange?.(selectedResources);
  }, [selectedResources]);

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

  const updatedSelectedResourceIds = (functionId, newSelectedResources) => {
    setSelectedResources((prevValues) => {
      if (newSelectedResources.length > 0) {
        return new Map(prevValues.set(functionId, newSelectedResources));
      } else if (prevValues.has(functionId)) {
        const newMap = new Map(prevValues);
        newMap.delete(functionId);
        return newMap;
      }
    });
  };

  const onExpandRow = async (expanded, record) => {
    const keys = [];
    if (expanded) {
      record.resources = resources;
      record.rowSelection = {
        selectedRowKeys: selectedResources?.get(record.function_id)?.map((resource) => resource.resource_id),
        onChange: (newSelectedResourceIds, newSelectedResources) => {
          updatedSelectedResourceIds(record.function_id, newSelectedResources);
        },
      };
      keys.push(record.function_id);
    }
    setExpandedKeys(keys);
  };

  const expandedRowRender = {
    expandedRowRender: (func) => {
      return <ResourceTable resources={resources} hasActions rowSelection={func.rowSelection}/>;
    },
    expandedRowKeys: expandedKeys,
    onExpand: onExpandRow,
  };

  return (
    <Table
      dataSource={functions}
      rowKey={(record) => record.function_id}
      expandable={isExpandable ? expandedRowRender : null}
      size="small"
    >
      <Column title="Id" dataIndex="function_id" key="id"
        sorter={(a, b) => a.function_id - b.function_id}
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
            {!hideDelete && (<Button onClick={() => showDeleteConfirm(record.function_id)} icon={<DeleteOutlined />}/>)}
          </Space>
        )}
      />
    </Table>
  );
};

FunctionTable.propTypes = {
  value: PropTypes.object,
  onChange: PropTypes.func,
  hideDelete: PropTypes.bool,
  isExpandable: PropTypes.bool,
  resources: PropTypes.arrayOf(PropTypes.object),
};

export default FunctionTable;
