import NewEntityContainer from '../../components/misc/NewEntityContainer';
import NewArtifactTypeForm from '../../components/artifacttypes/NewArtifactTypeForm';
import {useState} from 'react';
import PropTypes from 'prop-types';


const NewType = ({setError}) => {
  const [newServiceType, setNewServiceType] = useState(null);
  return <>
    <NewEntityContainer
      entityName="Service Type"
      isFinished={newServiceType != null}
      onReset={() => setNewServiceType(null)}
      rootPath="/services/services"
      overviewName="Service"
    >
      <NewArtifactTypeForm artifact='service' setNewArtifactType={setNewServiceType} setError={setError} />
    </NewEntityContainer>
  </>;
};

NewType.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewType;
