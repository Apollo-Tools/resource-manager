import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {Button, Form, Input, message, Select} from 'antd';
import {SettingOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {listRegions} from '../../lib/api/RegionService';
import {createVPC} from '../../lib/api/VPCService';
import ProviderIcon from '../misc/ProviderIcon';


const NewVPCForm = ({excludeRegions, setFinished}) => {
  const {token, checkTokenExpired} = useAuth();
  const [regions, setRegions] = useState([]);
  const [newVPC, setNewVPC] = useState();
  const [error, setError] = useState(false);
  const [messageApi, contextHolder] = message.useMessage();
  const [form] = Form.useForm();

  useEffect(() => {
    if (!checkTokenExpired()) {
      listRegions(token, setRegions, setError);
    }
  }, []);

  useEffect(() => {
    if (error) {
      messageApi.open({
        type: 'error',
        content: 'Something went wrong!',
      });
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (newVPC) {
      messageApi.open({
        type: 'success',
        content: 'VPC has been created successfully!',
      });
      setFinished(true);
    }
  }, [newVPC]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createVPC(values.vpcIdValue, values.subnetIdValue, values.region, token, setNewVPC, setError)
          .then(() => form.resetFields());
    }
  };

  return (
    <>
      {contextHolder}
      <Form
        name="vpcForm"
        onFinish={onFinish}
        autoComplete="off"
        layout="vertical"
        form={form}
      >
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
                <Select.Option
                  value={region.region_id}
                  key={region.region_id}
                  disabled={excludeRegions.includes(region.region_id)}>
                  <ProviderIcon provider={region.resource_provider.provider} className="mr-2"/>
                  {region.name}
                </Select.Option>
              );
            })}
          </Select>
        </Form.Item>

        <Form.Item
          label="VPC Id"
          name="vpcIdValue"
          rules={[
            {
              required: true,
              message: 'Please input the vpc id!',
            },
          ]}
        >
          <Input prefix={<SettingOutlined className="site-form-item-icon" />}/>
        </Form.Item>

        <Form.Item
          label="Subnet Id"
          name="subnetIdValue"
          rules={[
            {
              required: true,
              message: 'Please input the subnet id!',
            },
          ]}
        >
          <Input prefix={<SettingOutlined className="site-form-item-icon" />}/>
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

NewVPCForm.propTypes = {
  excludeRegions: PropTypes.arrayOf(PropTypes.number),
  setFinished: PropTypes.func,
};

export default NewVPCForm;
