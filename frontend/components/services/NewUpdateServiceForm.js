import {Button, Divider, Form, Input, InputNumber, Select, Space} from 'antd';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import PropTypes from 'prop-types';
import {createService, updateService} from '../../lib/ServiceService';
import {listK8sServiceTypes} from '../../lib/K8sServiceTypeService';
import {MinusCircleOutlined, PlusOutlined} from '@ant-design/icons';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import TooltipIcon from '../misc/TooltipIcon';
import {nameRegexValidationRule, nameValidationRule} from '../../lib/FormValidationRules';
import {listServiceTypes} from '../../lib/ServiceTypeService';


const NewUpdateServiceForm = ({setNewService, service, mode = 'new', setFinished}) => {
  const [form] = Form.useForm();
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState();
  const [k8sServiceTypes, setK8sServiceTypes] = useState([]);
  const [serviceTypes, setServiceTypes] = useState([]);
  const [initialPorts, setInitialPorts] = useState();
  const [selectedK8sServiceType, setSelectedK8sServiceType] = useState(null);
  const [isModified, setModified] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listServiceTypes(token, setServiceTypes, setError);
      listK8sServiceTypes(token, setK8sServiceTypes, setError);
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
    if (mode==='update' && k8sServiceTypes!=null && k8sServiceTypes.length>0) {
      setSelectedK8sServiceType(service.k8s_service_type);
    }
  }, [k8sServiceTypes]);

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
      const envVars = values.envVars != null ? values.envVars : [];
      const volumeMounts = values.volumeMounts != null ? values.volumeMounts : [];
      if (values.ports != null) {
        ports = values.ports.map((portEntries) => `${portEntries.containerPort}:${portEntries.servicePort}`);
      }
      if (mode === 'new') {
        await createService(values.serviceType, values.name, values.image, values.replicas, ports, values.cpu,
            values.memory, values.k8sServiceType, envVars, volumeMounts, token, setNewService, setError);
      } else {
        await updateService(service.service_id, values.replicas, ports, values.cpu, values.memory,
            values.k8sServiceType, envVars, volumeMounts, token, setError);
      }
      setFinished?.(true);
      setModified(false);
    }
  };
  const onFinishFailed = (errorInfo) => {
    console.log('Failed:', errorInfo);
  };

  const onChangeK8sServiceType = (value) => {
    const k8sServiceType = k8sServiceTypes.find((k8sServiceType) => k8sServiceType.service_type_id === value);
    setSelectedK8sServiceType(k8sServiceType ? k8sServiceType : null);
    setModified(true);
  };

  const onReset = () => {
    form.resetFields();
    if (mode==='update' && k8sServiceTypes!=null && k8sServiceTypes.length>0) {
      setSelectedK8sServiceType(service.service_type);
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
                label="Service Type"
                name="serviceType"
                rules={[
                  {
                    required: true,
                    message: 'Please select a service type!',
                  },
                ]}
                className="col-span-6"
              >
                <Select className="w-40">
                  {serviceTypes.map((serviceType) => {
                    return (
                      <Select.Option value={serviceType.artifact_type_id} key={serviceType.artifact_type_id} >
                        {serviceType.name}
                      </Select.Option>
                    );
                  })}
                </Select>
              </Form.Item>
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
              <TextDataDisplay label="Service Type" value={service.service_type.name} className="col-span-6"/>
              <TextDataDisplay label="Name" value={service.name} className="col-span-6"/>
              <TextDataDisplay label="Image" value={service.image} className="col-span-6"/>
              <TextDataDisplay label="Created at"
                value={<DateFormatter dateTimestamp={service.created_at} includeTime/>}
                className="col-span-6"/>
              <TextDataDisplay label="Updated at"
                value={<DateFormatter dateTimestamp={service.updated_at} includeTime/>}
                className="col-span-6"/>
            </>
          }
          <Divider className="lg:col-span-12 col-span-6"/>

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
            name="envList"
            label={<>
            Environment Variables
              <TooltipIcon text={
                <div className="m-0 text-start">
                  The environment variables for the service
                </div>
              } />
            </>}
            className="col-span-6 mb-0"
            rules={[{
              validator: () => {
                const values = form.getFieldValue(['envVars']) ?? [];
                if (new Set(values.map((envVar) => envVar.name)).size !== values.length) {
                  return Promise.reject(new Error('Duplicates are not allowed'));
                }
                return Promise.resolve();
              },
            }]}
          >
            <Form.List
              name="envVars"
              initialValue={service?.env_vars}
            >
              {(fields, {add, remove}) => (
                <>
                  <Form.Item className="mb-1">
                    <Button className="w-40" type="dashed"
                      onClick={() => {
                        add();
                        setModified(true);
                      }}
                      block
                      icon={<PlusOutlined />}
                    >
                      Add variable
                    </Button>
                  </Form.Item>
                  <div className="h-52 overflow-y-auto">
                    {fields.map(({index, key, name, ...field}) => (
                      <Space
                        key={key}
                        className="flex"
                        align="center mb-3"
                      >
                        <div>
                          <Form.Item
                            {...field}
                            name={[name, 'name']}
                            rules={[
                              {
                                required: true,
                                message: 'Missing name',
                              },
                            ]}
                            className="mb-0"
                          >
                            <Input addonBefore={<div className="w-14">Name</div>} className="w-60"/>
                          </Form.Item>
                          <Form.Item
                            {...field}
                            name={[name, 'value']}
                            rules={[
                              {
                                required: true,
                                message: 'Missing value',
                              },
                            ]}
                            className="mb-0"
                          >
                            <Input addonBefore={<div className="w-14">Value</div>} className="w-60"/>
                          </Form.Item>
                        </div>
                        <MinusCircleOutlined onClick={() => {
                          remove(name);
                          setModified(true);
                        }} />
                      </Space>
                    ))}
                  </div>
                </>
              )}
            </Form.List>
          </Form.Item>
          <Form.Item
            name="volumeMountList"
            label={<>
              Volume Mounts
              <TooltipIcon text={
                <div className="m-0 text-start">.
                  The volume mounts for the service
                </div>
              } />
            </>}
            rules={[{
              validator: () => {
                const values = form.getFieldValue(['volumeMounts']) ?? [];
                if (new Set(values.map((volumeMounts) => volumeMounts.name)).size !== values.length) {
                  return Promise.reject(new Error('Duplicates are not allowed'));
                }
                return Promise.resolve();
              },
            }]}
            className="col-span-6 mb-0"
          >
            <Form.List
              name="volumeMounts"
              initialValue={service?.volume_mounts}
            >
              {(fields, {add, remove}) => (
                <>
                  <Form.Item className="mb-1">
                    <Button className="w-40" type="dashed"
                      onClick={() => {
                        add();
                        setModified(true);
                      }}
                      block
                      icon={<PlusOutlined />}
                    >
                      Add volume mount
                    </Button>
                  </Form.Item>
                  <div className="h-52 overflow-y-auto">
                    {fields.map(({index, key, name, ...field}) => (
                      <Space
                        key={key}
                        className="flex mb-3"
                        align="center"
                      >
                        <div>
                          <Form.Item
                            {...field}
                            name={[name, 'name']}
                            rules={[
                              {
                                required: true,
                                message: 'Missing name',
                              },
                            ]}
                            className="mb-0"
                          >
                            <Input addonBefore={<div className="w-14">Name</div>} className="w-60"/>
                          </Form.Item>
                          <Form.Item
                            {...field}
                            name={[name, 'mount_path']}
                            rules={[
                              {
                                required: true,
                                message: 'Missing value',
                              },
                            ]}
                            className="mb-0"
                          >
                            <Input addonBefore={<div className="w-14">Mountpath</div>} className="w-60"/>
                          </Form.Item>
                          <Form.Item
                            {...field}
                            name={[name, 'size_megabytes']}
                            rules={[
                              {
                                required: true,
                                message: 'Missing size',
                              },
                            ]}
                            className="mb-0"
                          >
                            <InputNumber addonBefore={<div className="w-14">Size [MB]</div>} className="w-60" controls={false} min={1} precision={3}/>
                          </Form.Item>
                        </div>
                        <MinusCircleOutlined onClick={() => {
                          remove(name);
                          setModified(true);
                        }} />
                      </Space>
                    ))}
                  </div>
                </>
              )}
            </Form.List>
          </Form.Item>

          <Form.Item
            label={<>
                K8s Service Type
              <TooltipIcon text="the k8s service type" />
            </>}
            name="k8sServiceType"
            initialValue={service?.k8s_service_type.service_type_id}
            rules={[
              {
                required: true,
                message: 'Missing k8s service type',
              },
            ]}
            className="col-span-6 mb-0"
          >
            <Select className="w-40" onSelect={onChangeK8sServiceType}>
              {k8sServiceTypes.sort((st1, st2) => st1.name.localeCompare(st2.name))
                  .map((k8sServiceType) => {
                    return (
                      <Select.Option
                        value={k8sServiceType.service_type_id}
                        key={k8sServiceType.service_type_id}>
                        {k8sServiceType.name}
                      </Select.Option>
                    );
                  })}
            </Select>
          </Form.Item>

          {selectedK8sServiceType!=null && selectedK8sServiceType?.name !== 'NoService' &&
              <Form.Item
                name="portsList"
                rules={[{
                  validator: () => {
                    const values = form.getFieldValue(['ports']) ?? [];
                    if (!values || values.length === 0) {
                      return Promise.reject(new Error('Please add at least one port or select \'NoService\' as' +
                            ' Service Type'));
                    }
                    if (new Set(values.map((port) => `${port.containerPort}${port.servicePort}`)).size !== values.length) {
                      return Promise.reject(new Error('Duplicates are not allowed'));
                    }
                    return Promise.resolve();
                  },
                }]}
                label={<>
                    Ports
                  <TooltipIcon text={
                    <div className="m-0 text-start">
                        The mapping of container ports to service (external) ports.<br />
                        - Container Port = port exposed by container <br />
                        - Service Port = port exposed by service (external port)
                    </div>
                  } />
                </>}
                className="col-span-6 mb-0"
              >
                <Form.List
                  name="ports"
                  initialValue={initialPorts}
                >
                  {(fields, {add, remove}) => (
                    <>
                      <Form.Item className="mb-1">
                        <Button className="w-40" type="dashed"
                          onClick={() => {
                            add();
                            setModified(true);
                          }}
                          block
                          icon={<PlusOutlined />}
                        >
                          Add Port
                        </Button>
                      </Form.Item>
                      <div className="h-36 overflow-y-auto">
                        {fields.map(({index, key, name, ...field}) => (
                          <Space
                            key={key}
                            className="flex mb-3"
                            align="center"
                          >
                            <div>
                              <Form.Item
                                {...field}
                                name={[name, 'containerPort']}
                                rules={[
                                  {
                                    required: true,
                                    message: 'Missing container port',
                                  },
                                ]}
                                className="mb-0"
                              >
                                <InputNumber addonBefore={<div className="w-20">Container Port</div>} className="w-60" controls={false} min={1} precision={0}/>
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
                                className="mb-0"
                              >
                                <InputNumber addonBefore={<div className="w-20">Service Port</div>} className="w-60" controls={false} min={1} precision={0}/>
                              </Form.Item>
                            </div>
                            <MinusCircleOutlined onClick={() => {
                              remove(name);
                              setModified(true);
                            }}/>
                          </Space>
                        ))}
                      </div>
                    </>
                  )}
                </Form.List>
              </Form.Item>
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
        </div>

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
