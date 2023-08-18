import {useState} from 'react';
import NewEnsembleForm from '../../components/ensembles/NewEnsembleForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';


const NewEnsemble = () => {
  const [newEnsemble, setNewEnsemble] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Ensemble"
        isFinished={newEnsemble != null}
        onReset={() => setNewEnsemble(null)}
        rootPath="/ensembles/ensembles"
      >
        <NewEnsembleForm setNewEnsemble={setNewEnsemble}/>
      </NewEntityContainer>
    </>
  );
};

export default NewEnsemble;
