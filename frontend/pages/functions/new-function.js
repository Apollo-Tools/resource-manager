import {useState} from 'react';
import NewFunctionForm from '../../components/functions/NewFunctionForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';
import PropTypes from 'prop-types';


const NewFunction = ({setError}) => {
  const [newFunction, setNewFunction] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Function"
        isFinished={newFunction != null}
        onReset={() => setNewFunction(null)}
        rootPath="/functions/functions"
      >
        <NewFunctionForm setNewFunction={setNewFunction} setError={setError} />
      </NewEntityContainer>
    </>
  );
};

NewFunction.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default NewFunction;
