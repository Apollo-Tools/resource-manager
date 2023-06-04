import {Button, Form, Select} from 'antd';
import {createResource} from '../../lib/ResourceService';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {listRegions} from '../../lib/RegionService';
import ProviderIcon from '../misc/ProviderIcon';
import {listPlatforms} from '../../lib/PlatformService';


const NewResourceForm = ({setNewResource}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [platforms, setPlatforms] = useState([]);
  const [regions, setRegions] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listPlatforms(token, setPlatforms, setError)
          .then(() => setPlatforms((prevTypes) =>
            prevTypes.sort((a, b) => a.platform.localeCompare(b.platform)),
          ));
      listRegions(token, setRegions, setError)
          .then(() => setRegions((prevRegions) => {
            return prevRegions.sort((a, b) =>
              a.resource_provider.provider.localeCompare(b.resource_provider.provider) ||
                a.name.localeCompare(b.name));
          }));
    }
  }, []);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createResource(values.platform, values.region, token, setNewResource, setError);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
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
          label="Platform"
          name="platform"
          rules={[
            {
              required: true,
              message: 'Missing platform',
            },
          ]}
        >
          <Select className="w-40">
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
        >
          <Select className="w-40">
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
