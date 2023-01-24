import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Segmented, Typography} from 'antd';
import {getFunction} from '../../lib/FunctionService';
import {listRuntimes} from '../../lib/RuntimeService';
import {listFunctionResources} from '../../lib/FunctionResourceService';
import UpdateFunctionForm from '../../components/UpdateFunctionForm';
import AddFunctionResourcesForm from '../../components/AddFunctionResourcesForm';
import ResourceTable from '../../components/ResourceTable';

// TODO: add way to update values
const ResourceDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [func, setFunction] = useState('');
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [runtimes, setRuntimes] = useState([]);
  const [functionResources, setFunctionResources] = useState([]);
  const [isFinished, setFinished] = useState(false);
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getFunction(id, token, setFunction, setError);
      listRuntimes(token, setRuntimes, setError);
      listFunctionResources(id, token, setFunctionResources, setError);
    }
  }, [id]);

  useEffect(() => {
    if (isFinished) {
      listFunctionResources(id, token, setFunctionResources, setError)
          .then(() => setFinished(false));
    }
  }, [isFinished]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const reloadFunction = async () => {
    await getFunction(id, token, setFunction, setError);
  };

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Function Details ({func.function_id})</Typography.Title>
      <Divider />
      <Segmented options={['Details', 'Function Resources']} value={selectedSegment}
        onChange={(e) => setSelectedSegment(e)} size="large" block/>
      <Divider />
      {
        selectedSegment === 'Details' &&
        <UpdateFunctionForm func={func} runtimes={runtimes} reloadFunction={reloadFunction}/>
      }
      {
        selectedSegment === 'Function Resources' && (
          <>
            <div>
              {
                <ResourceTable />
              }
            </div>
            <Divider />
            <AddFunctionResourcesForm
              func={func}
              excludeResourceIds={functionResources.map((functionResource) => functionResource.resource_id)}
              setFinished={setFinished}
            />
          </>)
      }
    </div>
  );
};

export default ResourceDetails;
