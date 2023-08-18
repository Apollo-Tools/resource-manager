import {useState} from 'react';
import NewFunctionForm from '../../components/functions/NewFunctionForm';
import NewEntityContainer from '../../components/misc/NewEntityContainer';


const NewFunction = () => {
  const [newFunction, setNewFunction] = useState(null);

  return (
    <>
      <NewEntityContainer
        entityName="Function"
        isFinished={newFunction != null}
        onReset={() => setNewFunction(null)}
        rootPath="/functions/functions"
      >
        <NewFunctionForm setNewFunction={setNewFunction} />
      </NewEntityContainer>
    </>
  );
};

export default NewFunction;
