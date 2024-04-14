import {useState} from 'react';
import NewResourceForm from '../../components/resources/NewResourceForm';
import AddMetricValuesForm from '../../components/metrics/AddMetricValuesForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';
import PropTypes from 'prop-types';


const NewResource = ({setError}) => {
  const [newResource, setNewResource] = useState(null);
  const [isFinished, setFinished] = useState(false);

  const onReset = () => {
    setNewResource(null);
    setFinished(false);
  };

  return (
    <>
      <NewEntityContainer
        entityName="Resource"
        isFinished={isFinished}
        onReset={onReset}
        rootPath="/resources/resources"
      >
        {newResource ?
        <AddMetricValuesForm resource={newResource} setFinished={setFinished} isNewResource setError={setError} />:
        <NewResourceForm setNewResource={setNewResource} setError={setError}/>}
      </NewEntityContainer>
    </>
  );
};

NewResource.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewResource;
