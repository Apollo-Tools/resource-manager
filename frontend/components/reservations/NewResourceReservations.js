import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {getEnsemble} from '../../lib/EnsembleService';
import FunctionTable from '../functions/FunctionTable';
import ServiceTable from '../services/ServiceTable';
import {Button, Typography} from 'antd';


const NewResourceReservations = ({ensembleId, functionResources, setFunctionResources, serviceResources,
  setServiceResources, next, prev}) => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState([]);
  const [error, setError] = useState(false);
  const [selected, setSelected] = useState(false);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  useEffect(() => {
    if (ensembleId!= null && !checkTokenExpired()) {
      getEnsemble(ensembleId, token, setEnsemble, setError);
      setSelected(checkAnySelected);
    }
  }, []);

  const checkAnySelected = () => {
    return (functionResources != null && functionResources.size > 0) ||
    (serviceResources != null && serviceResources.size > 0);
  };

  const onFunctionResourcesSelected = (resources) => {
    setFunctionResources(resources);
    setSelected(checkAnySelected);
  };

  const onServiceResourcesSelected = (resources) => {
    setServiceResources(resources);
    setSelected(checkAnySelected);
  };

  const onClickBack = () => {
    setFunctionResources(new Map());
    setServiceResources(new Map());
    prev();
  };

  return (
    <>
      <Typography.Title level={2}>Functions</Typography.Title>
      <FunctionTable
        value={functionResources}
        hideDelete
        isExpandable
        resources={ensemble?.resources}
        onChange={onFunctionResourcesSelected}/>
      <Typography.Title level={2}>Services</Typography.Title>
      <ServiceTable
        value={serviceResources}
        hideDelete
        isExpandable
        resources={ensemble?.resources}
        onChange={onServiceResourcesSelected}
      />
      <Button type="primary" onClick={next} disabled={!selected} className="float-right">Next</Button>
      <Button type="default" onClick={onClickBack} className="float-left">Back</Button>
    </>
  );
};

export default NewResourceReservations;
