import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Modal, Segmented, Typography} from 'antd';
import ResourceTable from '../../components/resources/ResourceTable';
import {getEnsemble, validateEnsemble} from '../../lib/EnsembleService';
import EnsembleDetailsCard from '../../components/ensembles/EnsembleDetailsCard';
import {ExclamationCircleFilled, PlusCircleOutlined} from '@ant-design/icons';
import {addResourceToEnsemble, deleteResourceFromEnsemble} from '../../lib/ResourceEnsembleService';
import {listResourcesBySLOs} from '../../lib/ResourceService';

const {confirm} = Modal;

const EnsembleDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState();
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [error, setError] = useState(false);
  const [resourceToAdd, setResourcesToAdd] = useState([]);
  const [filteredResources, setFilteredResources] = useState([]);
  const [validatedResources, setValidatedResources] = useState([]);
  const [invalidResourceIds, setInvalidResourceIds] = useState([]);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getEnsemble(id, token, setEnsemble, setError);
    }
  }, [id]);

  useEffect(() => {
    if (!checkTokenExpired() && ensemble!=null) {
      listResourcesBySLOs(ensemble.slos, token, setResourcesToAdd, setError);
      validateEnsemble(id, token, setValidatedResources, setError);
    }
  }, [ensemble]);

  useEffect(() => {
    if (validatedResources != null) {
      setInvalidResourceIds(validatedResources
          .filter((entry) => !entry.is_valid)
          .map((entry) => entry.resource_id));
    }
  }, [validatedResources]);

  useEffect(() => {
    if (ensemble!=null && resourceToAdd.length >= 0) {
      setFilteredResources(() => {
        const existingResources = ensemble.resources.map((resource) => resource.resource_id);
        return resourceToAdd.filter((resource) => !existingResources.includes(resource.resource_id));
      });
    }
  }, [resourceToAdd]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const reloadEnsemble = async () => {
    if (!checkTokenExpired()) {
      await getEnsemble(id, token, setEnsemble, setError);
    }
  };

  const onDeleteEnsembleResource = (resourceId) => {
    if (!checkTokenExpired()) {
      deleteResourceFromEnsemble(id, resourceId, token, setError)
          .then(async (result) => {
            if (result) {
              await reloadEnsemble();
            }
          });
    }
  };

  const onAddEnsembleResource = (resourceId) => {
    if (!checkTokenExpired()) {
      addResourceToEnsemble(id, resourceId, token, null, setError)
          .then(async (result) => {
            if (result) {
              await reloadEnsemble();
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
        onDeleteEnsembleResource(id);
      },
    });
  };

  const showAddConfirm = (id) => {
    confirm({
      title: 'Confirmation',
      icon: <ExclamationCircleFilled />,
      content: 'Are you sure you want to add this resource to the ensemble?',
      okText: 'Yes',
      okType: 'danger',
      cancelText: 'No',
      onOk() {
        onAddEnsembleResource(id);
      },
    });
  };

  const setInvalidRowClasses = (resource) => {
    if (invalidResourceIds.includes(resource.resource_id)) {
      return 'invalid-entry';
    }
    return '';
  };

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Ensemble Details ({ensemble?.ensemble_id})</Typography.Title>
      <Divider />
      <Segmented options={['Details', 'Resources', 'Add Resources']} value={selectedSegment}
        onChange={(e) => setSelectedSegment(e)} size="large" block/>
      <Divider />
      {
        selectedSegment === 'Details' && ensemble &&
        <EnsembleDetailsCard ensemble={ensemble}/>
      }
      {
        selectedSegment === 'Resources' && ensemble && (
          <>
            <div>
              <ResourceTable
                resources={ensemble.resources}
                hasActions onDelete={showDeleteConfirm}
                getRowClassname={setInvalidRowClasses}
              />
            </div>
          </>)
      }
      {
        selectedSegment === 'Add Resources' && ensemble && invalidResourceIds && (
          <>
            <Typography.Title level={3}>Add Resources</Typography.Title>
            <ResourceTable
              resources={filteredResources}
              hasActions
              customButton={{icon: <PlusCircleOutlined />, onClick: showAddConfirm, tooltip: 'Add'}}
            />
          </>
        )
      }
    </div>
  );
};

export default EnsembleDetails;
