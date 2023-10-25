import PropTypes from 'prop-types';
import TextDataDisplay from '../misc/TextDataDisplay';
import DateFormatter from '../misc/DateFormatter';
import ProviderIcon from '../misc/ProviderIcon';
import {Button, Tooltip} from 'antd';
import {ClusterOutlined, LockTwoTone, UnlockTwoTone} from '@ant-design/icons';
import Link from 'next/link';
import BoolDataDisplay from '../misc/BoolDataDisplay';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {updateResource} from '../../lib/ResourceService';


const ResourceDetailsCard = ({resource, setResource}) => {
  const {token, checkTokenExpired} = useAuth();
  const [error, setError] = useState(false);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const setResourceLockable = (isLockable) => {
    if (!checkTokenExpired()) {
      updateResource(resource.resource_id, isLockable, token, setError)
          .then((result) => {
            if (result === true) {
              setResource((prevState) => ({
                ...prevState,
                is_lockable: isLockable,
              }));
            }
          });
    }
  };

  return (
    <div className="grid lg:grid-cols-12 grid-cols-6 gap-4">
      <TextDataDisplay label="Name" value={resource.name} className="col-span-6"/>
      <TextDataDisplay label="Environment" value={resource.region.resource_provider.environment.environment} className="col-span-6"/>
      <TextDataDisplay label="Resource Type" value={resource.platform.resource_type.resource_type} className="col-span-6"/>
      <TextDataDisplay label="Platform" value={resource.platform.platform} className="col-span-6"/>
      <TextDataDisplay label="Provider"
        value={<>
          <ProviderIcon provider={resource.region.resource_provider.provider} className="mr-1"/>
          {resource.region.resource_provider.provider}
        </>}
        className="col-span-6" />
      <TextDataDisplay label="Region" value={resource.region.name} className="col-span-6" />
      <TextDataDisplay label="Created at" value={<DateFormatter dateTimestamp={resource.created_at} includeTime/>} className="col-span-6"/>
      <div className="col-span-6 flex" >
        <BoolDataDisplay label="Lockable" value={resource.is_lockable} />
        {resource.is_lockable ?
              <Tooltip title="Set to non lockable" className="ml-5 self-end">
                <Button onClick={() => setResourceLockable(false)}
                  icon={<UnlockTwoTone twoToneColor={'DarkGrey'}/>}/>
              </Tooltip> :
              <Tooltip title="Set to lockable" className="ml-5 self-end">
                <Button onClick={() => setResourceLockable(true)}
                  icon={<LockTwoTone twoToneColor={'DarkGrey'}/>}/>
              </Tooltip>}
      </div>
      {resource.main_resource_id &&
        <Link href={`/resources/${resource.main_resource_id}`} className="col-span-full">
          <Button type="primary" icon={<ClusterOutlined />}>Main Resource</Button>
        </Link>}
    </div>
  );
};

ResourceDetailsCard.propTypes = {
  resource: PropTypes.object.isRequired,
  setResource: PropTypes.func.isRequired,
};

export default ResourceDetailsCard;
