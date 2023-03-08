import DateFormatter from '../misc/DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import Link from 'next/link';
import {DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined} from '@ant-design/icons';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteFunction, listFunctions} from '../../lib/FunctionService';
import {getFunctionResources} from '../../lib/FunctionResourceService';
import ResourceTable from '../resources/ResourceTable';
import PropTypes from 'prop-types';

const {Column} = Table;
const {confirm} = Modal;

const FunctionTable = ({value = {}, onChange, hideDelete, isExpandable}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [functions, setFunctions] = useState([]);
  const [selectedResourceIds, setSelectedResourceIds] = useState(new Map());

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

  useEffect(() => {
    onChange?.({selectedResourceIds, ...value, ...{selectedResourceIds}});
  }, [selectedResourceIds]);

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

  const updatedSelectedResourceIds = (functionId, newSelectedResourceIds) => {
    setSelectedResourceIds((prevValues) => {
      if (newSelectedResourceIds.length > 0) {
        return new Map(prevValues.set(functionId, newSelectedResourceIds));
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
      if (!Object.hasOwn(record, 'function_resources')) {
        await getFunctionResources(record.function_id, token, setError)
            .then((result) => {
              const rowSelection = {
                selectedResourceIds,
                onChange: (newSelectedResourceIds) => {
                  updatedSelectedResourceIds(record.function_id, newSelectedResourceIds);
                },
              };
              record.function_resources = result;
              record.rowSelection = rowSelection;
            });
      }
      keys.push(record.function_id);
    }
    setExpandedKeys(keys);
  };

  return (
    <Table
      dataSource={functions}
      rowKey={(record) => record.function_id}
      expandable={{
        expandedRowRender: (func) => {
          if (Object.hasOwn(func, 'function_resources') && func.function_resources.length > 0) {
            return (<ResourceTable resources={func.function_resources} hasActions rowSelection={func.rowSelection}/>);
          }
          return <p>No resources</p>;
        },
        rowExpandable: () => isExpandable,
        expandedRowKeys: expandedKeys,
        onExpand: onExpandRow,
      }}
    >
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
};

export default FunctionTable;
