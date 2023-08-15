import {useState} from 'react';
import NewResourceForm from '../../components/resources/NewResourceForm';
import AddMetricValuesForm from '../../components/metrics/AddMetricValuesForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';


const NewResource = () => {
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
        <AddMetricValuesForm resource={newResource} setFinished={setFinished} isNewResource />:
        <NewResourceForm setNewResource={setNewResource} />}
      </NewEntityContainer>
    </>
  );
};

export default NewResource;
