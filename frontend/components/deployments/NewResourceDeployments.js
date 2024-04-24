import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {getEnsemble} from '../../lib/api/EnsembleService';
import FunctionTable from '../functions/FunctionTable';
import ServiceTable from '../services/ServiceTable';
import {Button, Typography} from 'antd';
import PropTypes from 'prop-types';


const NewResourceDeployments = ({ensembleId, functionResources, setFunctionResources, serviceResources,
  setServiceResources, next, prev, setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState();
  const [isLoading, setLoading] = useState(true);
  const [selected, setSelected] = useState(false);
  const [functionResourceChoice, setfunctionResourceChoice] = useState([]);
  const [serviceResourceChoice, setServiceResourceChoice] = useState([]);

  useEffect(() => {
    if (ensembleId!= null && !checkTokenExpired()) {
      void getEnsemble(ensembleId, token, setEnsemble, setLoading, setError);
      setSelected(checkAnySelected);
    }
  }, []);

  useEffect(() => {
    if (ensemble != null) {
      setfunctionResourceChoice(ensemble?.resources
          .filter((resource) => resource.platform.resource_type.resource_type !== 'container'));
      setServiceResourceChoice(ensemble?.resources
          .filter((resource) => resource.platform.resource_type.resource_type === 'container'));
    }
  }, [ensemble]);

  useEffect(() => {
    setSelected(checkAnySelected);
  }, [functionResources, serviceResources]);

  const checkAnySelected = () => {
    return (functionResources != null && functionResources.size > 0) ||
    (serviceResources != null && serviceResources.size > 0);
  };

  const onClickBack = () => {
    setFunctionResources(new Map());
    setServiceResources(new Map());
    prev();
  };

  return (
    <>
      <Typography.Title level={2}>Functions</Typography.Title>
      {functionResourceChoice.length > 0 || isLoading ? <FunctionTable
        value={functionResources}
        hideDelete
        isExpandable
        resources={functionResourceChoice}
        onChange={setFunctionResources}
        allFunctions
        isLoading={isLoading}
        setError={setError} /> :
        <Typography.Text>
          No suitable resources for function deployments available...
        </Typography.Text>
      }
      <Typography.Title level={2}>Services</Typography.Title>
      {serviceResourceChoice.length > 0 || isLoading ? <ServiceTable
        value={serviceResources}
        hideDelete
        isExpandable
        resources={serviceResourceChoice}
        onChange={setServiceResources}
        allServices
        isLoading={isLoading}
        setError={setError}/> :
        <Typography.Text className="block mb-10">
          No suitable resources for service deployments available...
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
  setError: PropTypes.func.isRequired,
};

export default NewResourceDeployments;
