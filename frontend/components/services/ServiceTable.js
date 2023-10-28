import {Button, Modal, Space, Table, Tooltip} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled, InfoCircleOutlined, UserOutlined} from '@ant-design/icons';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import ResourceTable from '../resources/ResourceTable';
import PropTypes from 'prop-types';
import {deleteService, listAllServices, listMyServices} from '../../lib/api/ServiceService';
import ColumnFilterDropdown from '../misc/ColumnFilterDropdown';
import Link from 'next/link';
import DateColumnRender from '../misc/DateColumnRender';
import BoolValueDisplay from '../misc/BoolValueDisplay';

const {Column} = Table;
const {confirm} = Modal;

const ServiceTable = ({value = {}, onChange, hideDelete, isExpandable, resources, allServices = false}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedResources, setSelectedResources] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      if (allServices) {
        listAllServices(token, setServices, setError);
      } else {
        listMyServices(token, setServices, setError);
      }
      setSelectedResources(value);
    }
  }, [allServices]);

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
      deleteService(id, token, setError)
          .then((result) => {
            if (result) {
              setServices(services.filter((service) => service.service_id !== id));
            }
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this service?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onClickDelete(id);
      },
    });
  };

  const updatedSelectedResourceIds = (serviceId, newSelectedResources) => {
    setSelectedResources((prevValues) => {
      if (newSelectedResources.length > 0) {
        return new Map(prevValues.set(serviceId, newSelectedResources));
      } else if (prevValues.has(serviceId)) {
        const newMap = new Map(prevValues);
        newMap.delete(serviceId);
        return newMap;
      }
    });
  };

  const getSelectedRowKeys = (record) => {
    return selectedResources?.get(record.service_id)?.map((resource) => resource.resource_id);
  };

  const onExpandRow = async (expanded, record) => {
    const keys = [];
    if (expanded) {
      record.resources = resources;
      record.rowSelection = {
        selectedRowKeys: getSelectedRowKeys(record),
        onChange: (newSelectedResourceIds, newSelectedResources) => {
          record.rowSelection.selectedRowKeys = newSelectedResourceIds;
          updatedSelectedResourceIds(record.service_id, newSelectedResources);
        },
        getCheckboxProps: (resource) => ({
          disabled: resource.is_locked,
        }),
      };
      keys.push(record.service_id);
    }
    setExpandedKeys(keys);
  };

  const expandedRowRender = {
    expandedRowRender: (service) => {
      return (
        <ResourceTable resources={resources} hasActions rowSelection={service.rowSelection}/>
      );
    },
    expandedRowKeys: expandedKeys,
    onExpand: onExpandRow,
  };

  return (
    <Table
      dataSource={services}
      rowKey={(record) => record.service_id}
      expandable={isExpandable ? expandedRowRender : null}
      size="small"
    >
      <Column title="Id" dataIndex="service_id" key="id"
        sorter={(a, b) => a.service_id - b.service_id}
        defaultSortOrder="ascend"
      />
      <Column title="Type" dataIndex={['service_type', 'name']} key="service_type"
        sorter={(a, b) => a.service_type.name - b.service_type.name}
        defaultSortOrder="ascend"
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
        onFilter={(value, record) => record.service_type.name.startsWith(value)}
      />
      <Column title="Name" dataIndex="name" key="name"
        sorter={(a, b) =>
          a.name.localeCompare(b.name)}
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="name" />}
        onFilter={(value, record) => record.name.startsWith(value)}
      />
      <Column title="Image" dataIndex="image" key="image"
        sorter={(a, b) =>
          a.image.localeCompare(b.image)}
        filterDropdown={({setSelectedKeys, selectedKeys, confirm, clearFilters}) =>
          <ColumnFilterDropdown setSelectedKeys={setSelectedKeys} clearFilters={clearFilters}
            selectedKeys={selectedKeys} confirm={confirm} columnName="image" />}
        onFilter={(value, record) => record.image.startsWith(value)}
      />
      {allServices ?
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
        render={(createdAt) => <DateColumnRender value={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Updated at" dataIndex="updated_at" key="updated_at"
        render={(updatedAt) => <DateColumnRender value={updatedAt}/>}
        sorter={(a, b) => a.updated_at - b.updated_at}
      />
      {!hideDelete && <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            <Tooltip title="Details">
              <Link href={`/services/${record.service_id}`}>
                <Button icon={<InfoCircleOutlined />}/>
              </Link>
            </Tooltip>
            {!allServices && <Tooltip title="Delete">
              <Button onClick={() => showDeleteConfirm(record.service_id)} icon={<DeleteOutlined />}/>
            </Tooltip>}
          </Space>
        )}
      />}
    </Table>
  );
};

ServiceTable.propTypes = {
  value: PropTypes.object,
  onChange: PropTypes.func,
  hideDelete: PropTypes.bool,
  isExpandable: PropTypes.bool,
  resources: PropTypes.arrayOf(PropTypes.object),
  allServices: PropTypes.bool,
};

export default ServiceTable;
