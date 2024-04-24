import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {App, Button, Form, Input, Select} from 'antd';
import {SettingOutlined} from '@ant-design/icons';
import PropTypes from 'prop-types';
import {listRegions} from '../../lib/api/RegionService';
import {createVPC} from '../../lib/api/VPCService';
import ProviderIcon from '../misc/ProviderIcon';
import {successNotification} from '../../lib/misc/NotificationProvider';
import LoadingSpinner from '../misc/LoadingSpinner';


const NewVPCForm = ({excludeRegions, setFinished, isLoading, setError}) => {
  const {notification} = App.useApp();
  const {token, checkTokenExpired} = useAuth();
  const [regions, setRegions] = useState([]);
  const [isInsideLoading, setInsideLoading] = useState({listRegions: false, createVPC: false});
  const [form] = Form.useForm();

  const updateLoading = (type, newState) => {
    setInsideLoading((prevState) => {
      const newLoadings = {...prevState};
      newLoadings[type] = newState;
      return newLoadings;
    });
  };

  const setListRegionsLoading = (newState) => updateLoading('listRegions', newState);
  const setCreateVPCLoading = (newState) => updateLoading('createVPC', newState);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listRegions(token, setRegions, setListRegionsLoading, setError);
    }
  }, []);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      await createVPC(values.vpcIdValue, values.subnetIdValue, values.region, token, setCreateVPCLoading, setError)
          .then((result) => {
            if (result) {
              successNotification(notification, 'VPC has been created successfully!');
              setFinished(true);
              form.resetFields();
            }
          });
    }
  };

  if (isLoading || isInsideLoading['listRegions']) {
    return (<div className="h-64"><LoadingSpinner isCard={false}/></div>);
  }

  return (
    <>
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
          <Button type="primary" htmlType="submit" loading={isInsideLoading['createVPC']}>
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
  isLoading: PropTypes.bool.isRequired,
  setError: PropTypes.func.isRequired,
};

export default NewVPCForm;
