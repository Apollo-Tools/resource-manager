import PropTypes from 'prop-types';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import ProviderIcon from '../misc/ProviderIcon';
import {Button, Modal, Tooltip} from 'antd';
import {ClusterOutlined, ExclamationCircleFilled, LockTwoTone, UnlockTwoTone} from '@ant-design/icons';
import Link from 'next/link';
import BoolDataDisplay from '../misc/BoolDataDisplay';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useState} from 'react';
import {updateResource} from '../../lib/api/ResourceService';

const {confirm} = Modal;

const ResourceDetailsCard = ({resource, setResource, isLoading, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isInsideLoading, setInsideLoading] = useState(false);

  const setResourceLockable = (isLockable) => {
    if (!checkTokenExpired()) {
      updateResource(resource.resource_id, isLockable, token, setInsideLoading, setError)
          .then((result) => {
            if (result === true) {
              setResource((prevState) => ({
                ...prevState,
                is_lockable: isLockable,
                is_locked: false,
              }));
            }
          });
    }
  };

  const showUpdateConfirm = (isLockable) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: `Are you sure you want to make this resource ${isLockable ? '' : 'non-'}lockable? ${isLockable ? '' :
        'This will unlock the item if it is currently locked!'}`,
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        setResourceLockable(isLockable);
      },
    });
  };

  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <TextDataDisplay label="Name" value={resource?.name} className="col-span-6" isLoading={isLoading}/>
      <TextDataDisplay label="Environment" value={resource?.region.resource_provider.environment.environment}
        className="col-span-6" isLoading={isLoading}/>
      <TextDataDisplay label="Resource Type" value={resource?.platform.resource_type.resource_type}
        className="col-span-6" isLoading={isLoading}/>
      <TextDataDisplay label="Platform" value={resource?.platform.platform} className="col-span-6"
        isLoading={isLoading}/>
      <TextDataDisplay label="Provider"
        value={resource && <>
          <ProviderIcon provider={resource.region.resource_provider.provider} className="mr-1"/>
          {resource.region.resource_provider.provider}
        </>}
        className="col-span-6"
        isLoading={isLoading}
      />
      <TextDataDisplay label="Region" value={resource?.region.name} className="col-span-6" isLoading={isLoading} />
      <TextDataDisplay
        label="Created at"
        value={resource && <DateFormatter dateTimestamp={resource.created_at} includeTime/>}
        className="col-span-6"
        isLoading={isLoading}
      />
      <div className="col-span-6 flex">
        <BoolDataDisplay label="Lockable" value={resource ? resource.is_lockable : false} isLoading={isLoading} />
        {resource?.is_lockable ?
              <Tooltip title="Set to non lockable" className="ml-5 self-end">
                <Button
                  onClick={() => showUpdateConfirm(false)}
                  icon={<UnlockTwoTone twoToneColor={'DarkGrey'}/>}
                  loading={isInsideLoading}
                  disabled={isLoading}
                />
              </Tooltip> :
              <Tooltip title="Set to lockable" className="ml-5 self-end">
                <Button
                  onClick={() => showUpdateConfirm(true)}
                  icon={<LockTwoTone twoToneColor={'DarkGrey'}/>}
                  loading={isInsideLoading}
                  disabled={isLoading}
                />
              </Tooltip>}
      </div>
      {resource?.main_resource_id &&
        <Link href={`/resources/${resource.main_resource_id}`} className="col-span-full">
          <Button type="primary" icon={<ClusterOutlined />}>Main Resource</Button>
        </Link>}
    </div>
  );
};

ResourceDetailsCard.propTypes = {
  resource: PropTypes.object,
  setResource: PropTypes.func.isRequired,
  isLoading: PropTypes.bool.isRequired,
  setError: PropTypes.func.isRequired,
};

export default ResourceDetailsCard;
