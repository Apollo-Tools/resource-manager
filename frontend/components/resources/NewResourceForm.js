import {Button, Form, Input, Select, Switch} from 'antd';
import {createResource} from '../../lib/api/ResourceService';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import PropTypes from 'prop-types';
import ProviderIcon from '../misc/ProviderIcon';
import {listPlatforms, listRegionsByPlatform} from '../../lib/api/PlatformService';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/api/FormValidationRules';
import TooltipIcon from '../misc/TooltipIcon';
import {updateLoadingState} from '../../lib/misc/LoadingUtil';


const NewResourceForm = ({setNewResource, setError}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [isLoading, setLoading] = useState(
      {
        listPlatforms: true,
        createResource: false,
        listRegions: false,
      });
  const [platforms, setPlatforms] = useState([]);
  const [regions, setRegions] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listPlatforms(token, setPlatforms, updateLoadingState('listPlatforms', setLoading), setError);
    }
  }, []);

  useEffect(() => {
    setPlatforms((prevTypes) =>
      prevTypes.sort((a, b) => a.platform.localeCompare(b.platform)));
  }, [platforms]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createResource(values.name, values.platform, values.region, values.isLockable, token, setNewResource,
          updateLoadingState('createResource', setLoading), setError);
    }
  };

  const onChangePlatform = async (platformId) => {
    if (!checkTokenExpired()) {
      await listRegionsByPlatform(platformId, token, setRegions, updateLoadingState('listRegions', setLoading), setError);
    }
    form.resetFields(['region']);
  };

  return (
    <>
      <Form
        form={form}
        name="basic"
        onFinish={onFinish}
        autoComplete="off"
        layout="vertical"
        className="grid lg:grid-cols-12 grid-cols-6 gap-4"
      >
        <Form.Item
          label="Name"
          name="name"
          rules={[nameValidationRule, nameRegexValidationRule]}
          className="col-span-6"
        >
          <Input className="w-40" />
        </Form.Item>
        <Form.Item
          label={<>
              Is Lockable
            <TooltipIcon text="whether a resource is lockable for deployments or not" />
          </>}
          name="isLockable"
          valuePropName={'checked'}
          initialValue={false}
          className="col-span-6"
        >
          <Switch checkedChildren="true" unCheckedChildren="false" />
        </Form.Item>
        <Form.Item
          label="Platform"
          name="platform"
          rules={[
            {
              required: true,
              message: 'Missing platform',
            },
          ]}
          className="col-span-6"
        >
          <Select className="w-40" onChange={onChangePlatform} loading={isLoading['listPlatforms']}>
            {platforms.map((platform) => {
              return (
                <Select.Option value={platform.platform_id} key={platform.platform_id}>
                  <span>{platform.platform}</span> -
                  <span> ({platform.resource_type.resource_type})</span>
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
          className="col-span-6"
        >
          <Select className="w-40" disabled={regions.length === 0} loading={isLoading['listRegions']}>
            {regions.map((region) => {
              return (
                <Select.Option value={region.region_id} key={region.region_id}>
                  <ProviderIcon provider={region.resource_provider.provider} className="mr-1"/> {region.name}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>
        <Form.Item>
          <Button type="primary" htmlType="submit" loading={isLoading['createResource']}>
              Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewResourceForm.propTypes = {
  setNewResource: PropTypes.func.isRequired,
  setError: PropTypes.func.isRequired,
};

export default NewResourceForm;
