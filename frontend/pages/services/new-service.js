import {useState} from 'react';
import NewUpdateServiceForm from '../../components/services/NewUpdateServiceForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';


const NewService = () => {
  const [newService, setNewService] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Service"
        isFinished={newService != null}
        onReset={() => setNewService(null)}
        rootPath="/services/services"
      >
        <NewUpdateServiceForm setNewService={setNewService} />
      </NewEntityContainer>
    </>
  );
};

export default NewService;
