import DateFormatter from '../misc/DateFormatter';
import {Button, Modal, Space, Table} from 'antd';
import {DeleteOutlined, ExclamationCircleFilled} from '@ant-design/icons';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import ResourceTable from '../resources/ResourceTable';
import PropTypes from 'prop-types';
import {deleteService, listServices} from '../../lib/ServiceService';

const {Column} = Table;
const {confirm} = Modal;

const ServiceTable = ({value = {}, onChange, hideDelete, isExpandable, resources}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);
  const [expandedKeys, setExpandedKeys] = useState([]);
  const [services, setServices] = useState([]);
  const [selectedResources, setSelectedResources] = useState();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listServices(token, setServices, setError);
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

  const onExpandRow = async (expanded, record) => {
    const keys = [];
    if (expanded) {
      record.resources = resources;
      record.rowSelection = {
        selectedRowKeys: selectedResources?.get(record.service_id)?.map((resource) => resource.resource_id),
        onChange: (newSelectedResourceIds, newSelectedResources) => {
          updatedSelectedResourceIds(record.service_id, newSelectedResources);
        },
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
      <Column title="Name" dataIndex="name" key="name"
        sorter={(a, b) =>
          a.name.localeCompare(b.name)}
      />
      <Column title="Created at" dataIndex="created_at" key="created_at"
        render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
        sorter={(a, b) => a.created_at - b.created_at}
      />
      <Column title="Actions" key="action"
        render={(_, record) => (
          <Space size="middle">
            {!hideDelete && (<Button onClick={() => showDeleteConfirm(record.service_id)} icon={<DeleteOutlined />}/>)}
          </Space>
        )}
      />
    </Table>
  );
};

ServiceTable.propTypes = {
  value: PropTypes.object,
  onChange: PropTypes.func,
  hideDelete: PropTypes.bool,
  isExpandable: PropTypes.bool,
};

export default ServiceTable;
