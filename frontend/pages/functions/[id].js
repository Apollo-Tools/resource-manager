import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Segmented, Typography, Modal} from 'antd';
import {getFunction} from '../../lib/FunctionService';
import {deleteFunctionResource, listFunctionResources} from '../../lib/FunctionResourceService';
import UpdateFunctionForm from '../../components/functions/UpdateFunctionForm';
import AddFunctionResourcesForm from '../../components/functions/AddFunctionResourcesForm';
import ResourceTable from '../../components/resources/ResourceTable';
import {ExclamationCircleFilled} from '@ant-design/icons';

const {confirm} = Modal;

const FunctionDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [func, setFunction] = useState('');
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [functionResources, setFunctionResources] = useState([]);
  const [isFinished, setFinished] = useState(false);
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getFunction(id, token, setFunction, setError);
      listFunctionResources(id, token, setFunctionResources, setError);
    }
  }, [id]);

  useEffect(() => {
    if (isFinished && !checkTokenExpired()) {
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
    if (!checkTokenExpired()) {
      await getFunction(id, token, setFunction, setError);
    }
  };

  const onDeleteFunctionResource = (resourceId) => {
    if (!checkTokenExpired()) {
      deleteFunctionResource(id, resourceId, token, setError)
          .then((result) => {
            if (result) {
              setFunctionResources((prevResources) => {
                return prevResources.filter((resource) => resource.resource_id !== resourceId);
              });
            }
          });
    }
  };

  const showDeleteConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to delete this resource?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onDeleteFunctionResource(id);
      },
    });
  };

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Function Details ({func.function_id})</Typography.Title>
      <Divider />
      <Segmented options={['Details', 'Function Resources']} value={selectedSegment}
        onChange={(e) => setSelectedSegment(e)} size="large" block/>
      <Divider />
      {
        selectedSegment === 'Details' && func &&
        <UpdateFunctionForm func={func} reloadFunction={reloadFunction}/>
      }
      {
        selectedSegment === 'Function Resources' && func && (
          <>
            <div>
              <Typography.Title level={3}>Function Resources</Typography.Title>
              {
                <ResourceTable resources={functionResources} hasActions onDelete={showDeleteConfirm} />
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

export default FunctionDetails;
