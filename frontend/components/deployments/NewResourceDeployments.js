import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {getEnsemble} from '../../lib/EnsembleService';
import FunctionTable from '../functions/FunctionTable';
import ServiceTable from '../services/ServiceTable';
import {Button, Typography} from 'antd';
import PropTypes from 'prop-types';


const NewResourceDeployments = ({ensembleId, functionResources, setFunctionResources, serviceResources,
  setServiceResources, next, prev}) => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState();
  const [error, setError] = useState(false);
  const [selected, setSelected] = useState(false);
  const [functionResourceChoice, setfunctionResourceChoice] = useState([]);
  const [serviceResourceChoice, setserviceResourceChoice] = useState([]);

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

  useEffect(() => {
    if (ensemble != null) {
      setfunctionResourceChoice(ensemble?.resources
          .filter((resource) => resource.platform.resource_type.resource_type !== 'container'));
      setserviceResourceChoice(ensemble?.resources
          .filter((resource) => resource.platform.resource_type.resource_type === 'container'));
    }
  }, [ensemble]);

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
      {functionResourceChoice.length > 0 ? <FunctionTable
        value={functionResources}
        hideDelete
        isExpandable
        resources={functionResourceChoice}
        onChange={onFunctionResourcesSelected}/> :
        <Typography.Text>
          No suitable resources for function deployment available...
        </Typography.Text>
      }
      <Typography.Title level={2}>Services</Typography.Title>
      {serviceResourceChoice.length > 0 ? <ServiceTable
        value={serviceResources}
        hideDelete
        isExpandable
        resources={serviceResourceChoice}
        onChange={onServiceResourcesSelected}/> :
        <Typography.Text className="block mb-10">
          No suitable resources for service deployment available...
        </Typography.Text>
      }
      <Button type="primary" onClick={next} disabled={!selected} className="float-right">Next</Button>
      <Button type="default" onClick={onClickBack} className="float-left">Back</Button>
    </>
  );
};

NewResourceDeployments.propTypes = {
  ensembleId: PropTypes.number.isRequired,
  functionResources: PropTypes.instanceOf(Map).isRequired,
  setFunctionResources: PropTypes.func.isRequired,
  serviceResources: PropTypes.instanceOf(Map).isRequired,
  setServiceResources: PropTypes.func.isRequired,
  next: PropTypes.func.isRequired,
  prev: PropTypes.func.isRequired,
};

export default NewResourceDeployments;
