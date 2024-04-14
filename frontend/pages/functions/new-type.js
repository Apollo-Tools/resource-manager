import NewEntityContainer from '../../components/misc/NewEntityContainer';
import NewArtifactTypeForm from '../../components/artifacttypes/NewArtifactTypeForm';
import {useState} from 'react';
import PropTypes from 'prop-types';


const NewType = ({setError}) => {
  const [newFunctionType, setNewFunctionType] = useState(null);
  return <>
    <NewEntityContainer
      entityName="Function Type"
      isFinished={newFunctionType != null}
      onReset={() => setNewFunctionType(null)}
      rootPath="/functions/functions"
      overviewName="Function"
    >
      <NewArtifactTypeForm artifact='function' setNewArtifactType={setNewFunctionType} setError={setError}/>
    </NewEntityContainer>
  </>;
};

NewType.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewType;
