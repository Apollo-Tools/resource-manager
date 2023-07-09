import {Button, Divider, Form, Input, InputNumber, Select, Space, Typography} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {createService, updateService} from '../../lib/ServiceService';
import {listServiceTypes} from '../../lib/ServiceTypeService';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import TooltipIcon from '../misc/TooltipIcon';


const NewUpdateServiceForm = ({setNewService, service, mode = 'new', setFinished}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [serviceTypes, setServiceTypes] = useState([]);
  const [initialPorts, setInitialPorts] = useState();
  const [selectedServiceType, setSelectedServiceType] = useState(null);
  const [isModified, setModified] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listServiceTypes(token, setServiceTypes, setError);
    }
  }, []);

  useEffect(() => {
    if (service!=null) {
      setInitialPorts(() => service.ports.map((port) => {
        return {
          containerPort: port.split(':')[0],
          servicePort: port.split(':')[1],
        };
      }));
    }
  }, [service]);

  useEffect(() => {
    if (mode==='update' && serviceTypes!=null && serviceTypes.length>0) {
      setSelectedServiceType(service.service_type);
    }
  }, [serviceTypes]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onFinish = async (values) => {
    if (!checkTokenExpired()) {
      let ports = [];
      if (values.ports != null) {
        ports = values.ports.map((portEntries) => `${portEntries.containerPort}:${portEntries.servicePort}`);
      }
      if (mode === 'new') {
        await createService(values.name, values.image, values.replicas, ports, values.cpu, values.memory,
            values.serviceType, token, setNewService, setError);
      } else {
        await updateService(service.service_id, values.replicas, ports, values.cpu, values.memory,
            values.serviceType, token, setError);
      }
      setFinished?.(true);
      setModified(false);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onChangeServiceType = (value) => {
    const serviceType = serviceTypes.find((serviceType) => serviceType.service_type_id === value);
    setSelectedServiceType(serviceType ? serviceType : null);
    setModified(true);
  };

  const onReset = () => {
    form.resetFields();
    if (mode==='update' && serviceTypes!=null && serviceTypes.length>0) {
      setSelectedServiceType(service.service_type);
    }
    setModified(false);
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
        onChange={() => setModified(true)}
      >
        <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
          {mode === 'new' ?
            (<>
              <Form.Item
                label="Name"
                name="name"
                rules={[
                  {
                    required: true,
                    message: 'Please input a name!',
                  },
                ]}
                className="col-span-6"
              >
                <Input className="w-40" />
              </Form.Item>

              <Form.Item
                label={<>
                  Image
                  <TooltipIcon text="the title of the docker image (image:tag)" />
                </>}
                name="image"
                rules={[
                  {
                    required: true,
                    message: 'Please input an image!',
                  },
                ]}
                className="col-span-6"
              >
                <Input className="w-40" />
              </Form.Item>
            </>) :
            <>
              <TextDataDisplay label="Name" value={service.name} className="col-span-6"/>
              <TextDataDisplay label="Image" value={service.image} className="col-span-6"/>
              <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={service.created_at} />}
                className="col-span-6"/>
              <Divider className="col-span-12"/>
            </>
          }

          <Form.Item
            label={<>
              Replicas
              <TooltipIcon text="the amount of replicas to deploy" />
            </>}
            name="replicas"
            rules={[
              {
                required: true,
                message: 'Please input the amount of replicas!',
              },
            ]}
            initialValue={service?.replicas ?? 1}
            className="col-span-6"
          >
            <InputNumber className="w-40" controls={false} min={1} precision={0}/>
          </Form.Item>

          <Form.Item
            label={<>
              CPU
              <TooltipIcon text="required cpu resource units (xx.xxx)" />
            </>}
            name="cpu"
            rules={[
              {
                required: true,
                message: 'Please input the amount of cpu requirements!',
              },
            ]}
            className="col-span-6"
            initialValue={service?.cpu ?? 0.1}
          >
            <InputNumber className="w-40" controls={false} min={0.01} precision={3}/>
          </Form.Item>

          <Form.Item
            label={<>
              Memory
              <TooltipIcon text="required memory resource units in megabytes" />
            </>}
            name="memory"
            rules={[
              {
                required: true,
                message: 'Please input the amount of memory requirements!',
              },
            ]}
            className="col-span-6"
            initialValue={service?.memory ?? 128}
          >
            <InputNumber className="w-40" controls={false} min={8} precision={0}/>
          </Form.Item>

          <Form.Item
            label={<>
              Service Type
              <TooltipIcon text="the k8s service type" />
            </>}
            name="serviceType"
            initialValue={service?.service_type.service_type_id}
            rules={[
              {
                required: true,
                message: 'Missing service type',
              },
            ]}
            className="col-span-6"
          >
            <Select className="w-40" onSelect={onChangeServiceType}>
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

        {selectedServiceType!=null && selectedServiceType?.name !== 'NoService' &&
          <div className="col-span-6">
            <Typography.Text>Ports</Typography.Text>
            <TooltipIcon text={
              <div className="m-0 text-start">
                The mapping of container ports to service (external) ports.<br />
                - Container Port = port exposed by container <br />
                - Service Port = port exposed by service (external port)
              </div>
            } />
            <Form.Item
              name="portsList"
              rules={[{
                validator: () => {
                  const values = form.getFieldValue(['ports']) ?? [];
                  if (!values || values.length === 0) {
                    return Promise.reject(new Error('Please add at least one port or select \'NoService\' as' +
                      ' Service Type'));
                  }
                  return Promise.resolve();
                },
              }]}
            >
              <Form.List
                name="ports"
                initialValue={initialPorts}
              >
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
            </Form.Item>
          </div>
        }
        <Form.Item>
          <Space size={'large'}>
            <Button type="primary" htmlType="submit" disabled={!isModified && mode === 'update'}>
              {mode === 'new' ? 'Create' : 'Update'}
            </Button>
            {
              mode === 'update' &&
            (<Button type="default" disabled={!isModified} onClick={onReset}>
                Reset
            </Button>)
            }
          </Space>
        </Form.Item>
      </Form>
    </>
  );
};

NewUpdateServiceForm.propTypes = {
  setNewService: PropTypes.func.isRequired,
  service: PropTypes.object,
  mode: PropTypes.oneOf(['new', 'update']),
  setFinished: PropTypes.func,
};

export default NewUpdateServiceForm;
