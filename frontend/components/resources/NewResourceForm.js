import {Button, Form, Select} from 'antd';
import {createResource} from '../../lib/ResourceService';
import {useEffect, useState} from 'react';
import {listResourceTypes} from '../../lib/ResourceTypeService';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {listRegions} from '../../lib/RegionService';
import ProviderIcon from '../misc/ProviderIcon';


const NewResourceForm = ({setNewResource}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [resourceTypes, setResourceTypes] = useState([]);
  const [regions, setRegions] = useState([]);
  const [regionSelectables, setRegionSelectables] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listResourceTypes(token, setResourceTypes, setError)
          .then(() => setResourceTypes((prevTypes) =>
            prevTypes.sort((a, b) => a.resource_type.localeCompare(b.resource_type)),
          ));
      listRegions(token, setRegions, setError)
          .then(() => setRegions((prevRegions) => {
            return prevRegions.sort((a, b) =>
              a.resource_provider.provider.localeCompare(b.resource_provider.provider) ||
                a.name.localeCompare(b.name));
          }));
    }
  }, []);

  useEffect(() => {
    setRegionSelectables(regions);
  }, [regions]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createResource(values.resourceType, false, values.region, token, setNewResource, setError);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onChangeResourceType = (typeId) => {
    if (resourceTypes.filter((type) => type.type_id === typeId)[0].resource_type === 'edge') {
      setRegionSelectables(() => regions.filter((region) => region.name === 'edge'));
    } else if (resourceTypes.filter((type) => type.type_id === typeId)[0].resource_type === 'container') {
      setRegionSelectables(() => regions.filter((region) => region.name === 'k8s'));
    } else {
      setRegionSelectables(() => regions.filter((region) => !['edge', 'k8s'].includes(region.name)));
    }
    form.resetFields(['region']);
  };

  return (
    <>
      <Form
        form={form}
        name="basic"
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <Form.Item
          label="Resource Type"
          name="resourceType"
          rules={[
            {
              required: true,
              message: 'Missing resource type',
            },
          ]}
        >
          <Select className="w-40" onChange={onChangeResourceType}>
            {resourceTypes.map((resourceType) => {
              return (
                <Select.Option value={resourceType.type_id} key={resourceType.type_id}>
                  {resourceType.resource_type}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        <Form.Item
          label="Region"
          name="region"
          rules={[
            {
              required: true,
              message: 'Missing region',
            },
          ]}
        >
          <Select className="w-40" disabled={form.getFieldValue(['resourceType']) === undefined}>
            {regionSelectables.map((region) => {
              return (
                <Select.Option value={region.region_id} key={region.region_id}>
                  <ProviderIcon provider={region.resource_provider.provider} className="mr-1"/> {region.name}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        <Form.Item>
          <Button type="primary" htmlType="submit">
                        Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewResourceForm.propTypes = {
  setNewResource: PropTypes.func.isRequired,
};

export default NewResourceForm;
