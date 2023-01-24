import {Button, Form, Table, Typography} from 'antd';
import {InfoCircleOutlined} from '@ant-design/icons';
import {useEffect, useState} from 'react';
import {useAuth} from '../lib/AuthenticationProvider';
import {listResources} from '../lib/ResourceService';
import PropTypes from 'prop-types';
import DateFormatter from './DateFormatter';
import Link from 'next/link';
import {addFunctionResources} from '../lib/FunctionResourceService';

const {Column} = Table;

const AddFunctionResourcesForm = ({
  func,
  excludeResourceIds,
}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [resources, setResources] = useState([]);
  const [selectedResourceIds, setSelectedResourceIds] = useState([]);
  const [error, setError] = useState(false);
  Form.useWatch('basic', form);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResources(false, token, setResources, setError)
          .then(() => {
            setResources((prevResources) => {
              let filteredResources = prevResources;
              if (excludeResourceIds != null) {
                filteredResources = prevResources
                    .filter((resource) => !excludeResourceIds.includes(resource.resource_id));
              }
              console.log(filteredResources);
              return filteredResources;
            });
          });
    }
  }, [excludeResourceIds]);


  const onClickAdd = async () => {
    if (!checkTokenExpired()) {
      const resources = selectedResourceIds.map((resourceId) => {
        return {resource_id: resourceId};
      });
      await addFunctionResources(func.function_id, resources, token, setError);
    }
  };

  const rowSelection = {
    selectedResourceIds,
    onChange: (newSelectedResourceIds) => {
      setSelectedResourceIds(newSelectedResourceIds);
    },
  };

  if (resources.length === 0) {
    return (<></>);
  }

  return (
    <>
      <Typography.Title level={3}>Add Resources</Typography.Title>
      <Table dataSource={resources} rowKey={(record) => record.resource_id}
        rowSelection={{type: 'checkbox', ...rowSelection}}
      >
        <Column title="Id" dataIndex="resource_id" key="id"
          sorter={(a, b) => a.resource_id - b.resource_id}
          defaultSortOrder="ascend"
        />
        <Column title="Type" dataIndex="resource_type" key="resource_type"
          render={(resourceType) => resourceType.resource_type}
          sorter={(a, b) =>
            a.resource_type.resource_type.localeCompare(b.resource_type.resource_type)}
        />
        <Column title="Self managed" dataIndex="is_self_managed" key="is_self_managed"
          render={(isSelfManaged) => isSelfManaged.toString()}
        />
        <Column title="Created at" dataIndex="created_at" key="created_at"
          render={(createdAt) => <DateFormatter dateTimestamp={createdAt}/>}
          sorter={(a, b) => a.created_at - b.created_at}
        />
        <Column title="Actions" key="action"
          render={(_, record) => (
            <Link href={`/resources/${record.resource_id}`}>
              <Button icon={<InfoCircleOutlined />}/>
            </Link>
          )}
        />
      </Table>
      <Button disabled={selectedResourceIds.length <= 0 } type="primary" onClick={onClickAdd}>Add Resources</Button>
    </>
  );
};

AddFunctionResourcesForm.propTypes = {
  func: PropTypes.object.isRequired,
  excludeResourceIds: PropTypes.arrayOf(PropTypes.number.isRequired),
  setFinished: PropTypes.func.isRequired,
  isSkipable: PropTypes.bool,
};

export default AddFunctionResourcesForm;
