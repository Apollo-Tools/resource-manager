import {Button, Form, Input, InputNumber, Select, Space, Typography} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {createService} from '../../lib/ServiceService';
import {listServiceTypes} from '../../lib/ServiceTypeService';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';


const NewServiceForm = ({setNewService}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [serviceTypes, setServiceTypes] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listServiceTypes(token, setServiceTypes, setError);
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
      const ports = values.ports.map((portEntries) => `${portEntries.servicePort}:${portEntries.containerPort}`);
      await createService(values.name, values.image, values.replicas, ports, values.cpu, values.memory,
          values.serviceType, token, setNewService, setError);
      console.log(values);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  return (
    <>
      <Form
        name="newServiceForm"
        form={form}
        onFinish={onFinish}
        onFinishFailed={onFinishFailed}
        autoComplete="off"
        layout="vertical"
      >
        <div className="grid md:grid-cols-2 grid-cols-1 gap-2 max-w-3xl">
          <Form.Item
            label="Name"
            name="name"
            rules={[
              {
                required: true,
                message: 'Please input a name!',
              },
            ]}
          >
            <Input className="w-40" />
          </Form.Item>

          <Form.Item
            label="Image"
            name="image"
            rules={[
              {
                required: true,
                message: 'Please input an image!',
              },
            ]}
          >
            <Input className="w-40" />
          </Form.Item>

          <Form.Item
            label="Replicas"
            name="replicas"
            rules={[
              {
                required: true,
                message: 'Please input the amount of replicas!',
              },
            ]}
            initialValue={1}
          >
            <InputNumber className="w-40" controls={false} min={1} precision={0}/>
          </Form.Item>

          <Form.Item
            label="CPU"
            name="cpu"
            rules={[
              {
                required: true,
                message: 'Please input the amount of cpu requirements!',
              },
            ]}
            initialValue={0.100}
          >
            <InputNumber className="w-40" controls={false} min={0.01} precision={3}/>
          </Form.Item>

          <Form.Item
            label="Memory"
            name="memory"
            rules={[
              {
                required: true,
                message: 'Please input the amount of memory requirements!',
              },
            ]}
            initialValue={128}
          >
            <InputNumber className="w-40" controls={false} min={8} precision={0}/>
          </Form.Item>

          <Form.Item
            label="Service Type"
            name="serviceType"
            rules={[
              {
                required: true,
                message: 'Missing service type',
              },
            ]}
          >
            <Select className="w-40">
              {serviceTypes.map((serviceType) => {
                return (
                  <Select.Option
                    value={serviceType.service_type_id}
                    key={serviceType.service_type_id}>
                    {serviceType.name}
                  </Select.Option>
                );
              })}
            </Select>
          </Form.Item>
        </div>

        <Typography.Text>Ports</Typography.Text>
        <Form.List name="ports">
          {(fields, {add, remove}) => (
            <>
              {fields.map(({index, key, name, ...field}) => (
                <Space
                  key={key}
                  className="flex"
                  align="baseline"
                >
                  <Form.Item
                    {...field}
                    name={[name, 'servicePort']}
                    rules={[
                      {
                        required: true,
                        message: 'Missing service port',
                      },
                    ]}
                  >
                    <InputNumber addonBefore="Service Port" className="w-40" controls={false} min={1} precision={0}/>
                  </Form.Item>
                  <Form.Item
                    {...field}
                    name={[name, 'containerPort']}
                    rules={[
                      {
                        required: true,
                        message: 'Missing container port',
                      },
                    ]}
                  >
                    <InputNumber addonBefore="Container Port" className="w-40" controls={false} min={1} precision={0}/>
                  </Form.Item>
                  <MinusCircleOutlined onClick={() => remove(name)} />
                </Space>
              ))}
              <Form.Item>
                <Button className="w-40" type="dashed" onClick={() => add()} block icon={<PlusOutlined />}>
                  Add Port
                </Button>
              </Form.Item>
            </>
          )}
        </Form.List>


        <Form.Item>
          <Button type="primary" htmlType="submit">
            Create
          </Button>
        </Form.Item>
      </Form>
    </>
  );
};

NewServiceForm.propTypes = {
  setNewService: PropTypes.func.isRequired,
};

export default NewServiceForm;
