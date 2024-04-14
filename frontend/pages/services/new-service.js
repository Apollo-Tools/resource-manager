import {useState} from 'react';
import NewUpdateServiceForm from '../../components/services/NewUpdateServiceForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';
import PropTypes from 'prop-types';


const NewService = ({setError}) => {
  const [newService, setNewService] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Service"
        isFinished={newService != null}
        onReset={() => setNewService(null)}
        rootPath="/services/services"
      >
        <NewUpdateServiceForm setNewService={setNewService} setError={setError}/>
      </NewEntityContainer>
    </>
  );
};

NewService.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewService;
