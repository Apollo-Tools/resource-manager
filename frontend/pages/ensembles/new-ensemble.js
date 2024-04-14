import {useState} from 'react';
import NewEnsembleForm from '../../components/ensembles/NewEnsembleForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';
import PropTypes from 'prop-types';


const NewEnsemble = ({setError}) => {
  const [newEnsemble, setNewEnsemble] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Ensemble"
        isFinished={newEnsemble != null}
        onReset={() => setNewEnsemble(null)}
        rootPath="/ensembles/ensembles"
      >
        <NewEnsembleForm setNewEnsemble={setNewEnsemble} setError={setError}/>
      </NewEntityContainer>
    </>
  );
};

NewEnsemble.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewEnsemble;
