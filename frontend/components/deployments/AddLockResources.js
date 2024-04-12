import {Button} from 'antd';
import {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import ResourceTable from '../resources/ResourceTable';
import NothingToSelectCard from './NothingToSelectCard';


const AddLockResources = ({functionResources, serviceResources, lockResources, setLockResources, next, prev}) => {
  const [lockableResources, setLockableResources] = useState([]);

  useEffect(() => {
    const resourceMap = new Map();
    mapResourceDeploymentsToResourceMap(functionResources, resourceMap);
    mapResourceDeploymentsToResourceMap(serviceResources, resourceMap);
    setLockableResources(Array.from(resourceMap.values()).sort((a, b) => a - b));
  }, []);

  const mapResourceDeploymentsToResourceMap = (resourceDeployments, resourceMap) => {
    Array.from(resourceDeployments.values()).flat()
        .filter((resource) => resource.is_lockable)
        .forEach((resource) => {
          resourceMap.set(resource.resource_id, resource);
        });
  };

  const onClickBack = () => {
    setLockResources([]);
    prev();
  };

  return (
    <>
      {lockableResources.length > 0 ?
        <ResourceTable resources={lockableResources} hasActions={true} resourceType="sub" rowSelection={
          {
            selectedRowKeys: lockResources,
            onChange: (newSelectedResourceIds) => {
              setLockResources(() => newSelectedResourceIds);
            },
          }
        }/> :
        <NothingToSelectCard text="No lockable resources selected"/>
      }
      <Button type="primary" onClick={next} className="float-right">Next</Button>
      <Button type="default" onClick={onClickBack} className="float-left">Back</Button>
    </>
  );
};

AddLockResources.propTypes = {
  functionResources: PropTypes.instanceOf(Map).isRequired,
  serviceResources: PropTypes.instanceOf(Map).isRequired,
  lockResources: PropTypes.arrayOf(PropTypes.number).isRequired,
  setLockResources: PropTypes.func.isRequired,
  next: PropTypes.func.isRequired,
  prev: PropTypes.func.isRequired,
};

export default AddLockResources;
