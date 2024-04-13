import EnsembleTable from '../ensembles/EnsembleTable';
import {Button, Divider, Form, Input, Switch} from 'antd';
import {useState} from 'react';
import PropTypes from 'prop-types';

const NewDeploymentEnsemble = ({selectedEnsemble, alertingUrl, next, setSelectedEnsemble, setAlertingUrl}) => {
  const [selected, setSelected] = useState(selectedEnsemble!=null);
  const [alerting, enableAlerting] = useState(alertingUrl != null);

  const rowSelection = {
    selectedRowKeys: [selectedEnsemble],
    onChange: (selectedRowKeys) => {
      setSelected(true);
      setSelectedEnsemble(() => selectedRowKeys[0]);
    },
    type: 'radio',
  };

  const onChangeAlerting = () => {
    enableAlerting((prev) => !prev);
  };

  const onFinish = async (values) => {
    if (values.enableAlerting) {
      setAlertingUrl(values.alertNotificationUrl);
    } else {
      setAlertingUrl(null);
    }
    next();
  };

  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const isURL = (rule, value) => {
    if (!value) {
      return Promise.resolve();
    }
    try {
      new URL(value);
      return Promise.resolve();
    } catch (error) {
      return Promise.reject(new Error('Please enter a valid URL'));
    }
  };

  return <>
    <Form
      name="newDeploymentEnsembleForm"
      onFinish={onFinish}
      onFinishFailed={onFinishFailed}
      autoComplete="off"
      layout="vertical"
    >
      <div className="grid lg:grid-cols-12 grid-cols-6 gap-4 mt-10 ml-10">
        <Form.Item
          className="col-span-6"
          label="Enable alerting"
          name="enableAlerting"
          valuePropName={'checked'}
          initialValue={alertingUrl != null}
        >
          <Switch
            checkedChildren="true"
            unCheckedChildren="false"
            checked={alerting} onChange={onChangeAlerting}/>
        </Form.Item>
        <Form.Item
          className="col-span-6"
          label="Alert Notification URL"
          name="alertNotificationUrl"
          initialValue={alertingUrl?.toString()}
          rules={[
            {
              required: alerting,
              message: 'Please provide the alert notification url!',
            },
            {
              validator: isURL,
            },
          ]}
          dependencies={['enableAlerting']}
        >
          <Input className="w-40" disabled={!alerting}/>
        </Form.Item>
      </div>
      <Divider />
      <EnsembleTable rowSelection={rowSelection}/>
      <Form.Item>
        <Button type="primary" disabled={!selected} className="float-right" htmlType="submit">Next</Button>
      </Form.Item>
    </Form>
  </>;
};

NewDeploymentEnsemble.propTypes = {
  selectedEnsemble: PropTypes.number,
  alertingUrl: PropTypes.string,
  next: PropTypes.func.isRequired,
  setSelectedEnsemble: PropTypes.func.isRequired,
  setAlertingUrl: PropTypes.func.isRequired,
};

export default NewDeploymentEnsemble;
