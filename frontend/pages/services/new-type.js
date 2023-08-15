import NewEntityContainer from '../../components/misc/NewEntityContainer';
import NewArtifactTypeForm from '../../components/artifacttypes/NewArtifactTypeForm';
import {useState} from 'react';


const NewType = () => {
  const [newServiceType, setNewServiceType] = useState(null);
  return <>
    <NewEntityContainer
      entityName="Service Type"
      newEntity={newServiceType}
      setNewEntity={setNewServiceType}
      rootPath="/services/services"
      overviewName="Service"
    >
      <NewArtifactTypeForm artifact='service' setNewArtifactType={setNewServiceType} />
    </NewEntityContainer>
  </>;
};

export default NewType;
