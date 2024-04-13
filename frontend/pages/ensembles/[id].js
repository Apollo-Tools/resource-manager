import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {Divider, Modal, Segmented, Typography} from 'antd';
import ResourceTable from '../../components/resources/ResourceTable';
import {getEnsemble, validateEnsemble} from '../../lib/api/EnsembleService';
import EnsembleDetailsCard from '../../components/ensembles/EnsembleDetailsCard';
import {ExclamationCircleFilled, PlusCircleOutlined} from '@ant-design/icons';
import {addResourceToEnsemble, deleteResourceFromEnsemble} from '../../lib/api/ResourceEnsembleService';
import {listResourcesBySLOs} from '../../lib/api/ResourceService';
import Head from 'next/head';
import {siteTitle} from '../../components/misc/Sidebar';
import PropTypes from 'prop-types';

const {confirm} = Modal;

const EnsembleDetails = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState();
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [isLoading, setLoading] = useState(false);
  const [resourceToAdd, setResourcesToAdd] = useState([]);
  const [filteredResources, setFilteredResources] = useState([]);
  const [validatedResources, setValidatedResources] = useState([]);
  const [invalidResourceIds, setInvalidResourceIds] = useState([]);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      void getEnsemble(id, token, setEnsemble, setLoading, setError);
    }
  }, [id]);

  useEffect(() => {
    if (!checkTokenExpired() && ensemble!=null) {
      void listResourcesBySLOs(ensemble.slos, token, setResourcesToAdd, setError);
      void validateEnsemble(id, token, setValidatedResources, setLoading, setError);
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

  const reloadEnsemble = async () => {
    if (!checkTokenExpired()) {
      await getEnsemble(id, token, setEnsemble, setLoading, setError);
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
      addResourceToEnsemble(id, resourceId, token, setError)
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
    } else if (resource.is_locked) {
      return 'locked-entry';
    }
    return 'available-entry';
  };

  return (
    <>
      <Head>
        <title>{`${siteTitle}: Ensemble Details`}</title>
      </Head>
      <div className="default-card">
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
                resourceType="ensemble"
                customButton={{icon: <PlusCircleOutlined />, onClick: showAddConfirm, tooltip: 'Add'}}
              />
            </>
          )
        }
      </div>
    </>
  );
};

EnsembleDetails.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default EnsembleDetails;
