import NewEntityContainer from '../../components/misc/NewEntityContainer';
import NewArtifactTypeForm from '../../components/artifacttypes/NewArtifactTypeForm';
import {useState} from 'react';


const NewType = () => {
  const [newFunctionType, setNewFunctionType] = useState(null);
  return <>
    <NewEntityContainer
      entityName="Function Type"
      newEntity={newFunctionType}
      setNewEntity={setNewFunctionType}
      rootPath="/functions/functions"
      overviewName="Function"
    >
      <NewArtifactTypeForm artifact='function' setNewArtifactType={setNewFunctionType} />
    </NewEntityContainer>
  </>;
};

export default NewType;
