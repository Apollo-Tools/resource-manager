import {Button, Modal, Space, Table, Tooltip} from 'antd';
import Link from 'next/link';
import {DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined, UserOutlined} from '@ant-design/icons';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteFunction, listAllFunctions, listMyFunctions} from '../../lib/api/FunctionService';
import ResourceTable from '../resources/ResourceTable';
import PropTypes from 'prop-types';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';
import RuntimeIcon from '../misc/RuntimeIcon';
import DateColumnRender from '../misc/DateColumnRender';
import BoolValueDisplay from '../misc/BoolValueDisplay';

const {Column} = Table;
const {confirm} = Modal;

const FunctionTable = ({value = {}, onChange, hideDelete, isExpandable, resources, allFunctions = false}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [functions, setFunctions] = useState([]);
  const [selectedResources, setSelectedResources] = useState(new Map());

  useEffect(() => {
    if (!checkTokenExpired()) {
      if (allFunctions) {
        listAllFunctions(token, setFunctions, setError);
      } else {
        listMyFunctions(token, setFunctions, setError);
      }
      setSelectedResources(value);
    }
  }, [allFunctions]);

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

  const getSelectedRowKeys = (record) => {
    return selectedResources?.get(record.function_id)?.map((resource) => resource.resource_id);
  };

  const onExpandRow = async (expanded, record) => {
    const keys = [];
    if (expanded) {
      record.resources = resources;
      record.rowSelection = {
        selectedRowKeys: getSelectedRowKeys(record),
        onChange: (newSelectedResourceIds, newSelectedResources) => {
          record.rowSelection.selectedRowKeys = newSelectedResourceIds;
          updatedSelectedResourceIds(record.function_id, newSelectedResources);
        },
        getCheckboxProps: (resource) => ({
          disabled: resource.is_locked,
        }),
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
      <Column title="Type" dataIndex={['function_type', 'name']} key="function_type"
        sorter={(a, b) => a.function_type.name - b.function_type.name}
        defaultSortOrder="ascend"
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
        onFilter={(value, record) => record.function_type.name.startsWith(value)}
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
        render={(runtime) =><>
          <RuntimeIcon runtime={runtime.name} className="mr-2"/>
          {runtime.name}
        </>}
        sorter={(a, b) =>
          a.runtime.name.localeCompare(b.runtime.name)}
      />
      {allFunctions ?
        <Column title="Created by" dataIndex="created_by" key="created_by"
          render={(createdBy) => <div><UserOutlined /> {createdBy?.username}</div> }
          sorter={(a, b) => a.created_by.username.localeCompare(b.created_by.username)}
          filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
            <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
              selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
          onFilter={(value, record) => record.created_by.username.startsWith(value)}
        /> :
        <Column title="Is Public" dataIndex="is_public" key="is_public"
          render={(isPublic) => <BoolValueDisplay value={isPublic} />}
        />
      }
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateColumnRender value={createdAt}/> }
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Updated at" dataIndex="updated_at" key="updated_at"
        render={(updatedAt) => <DateColumnRender value={updatedAt}/> }
        sorter={(a, b) => a.updated_at - b.updated_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={`/functions/${record.function_id}`}>
                <Button icon={<InfoCircleOutlined />}/>
              </Link>
            </Tooltip>
            {!hideDelete && !allFunctions && (
              <Tooltip title="Delete">
                <Button onClick={() => showDeleteConfirm(record.function_id)} icon={<DeleteOutlined />}/>
              </Tooltip>)}
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
  allFunctions: PropTypes.bool,
};

export default FunctionTable;
