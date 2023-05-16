import {useRouter} from 'next/router';
import {useEffect, useState} from 'react';
import {useAuth} from '../../lib/AuthenticationProvider';
import {Divider, Segmented, Typography} from 'antd';
import ResourceTable from '../../components/resources/ResourceTable';
import {getEnsemble} from '../../lib/EnsembleService';
import EnsembleDetailsCard from '../../components/ensembles/EnsembleDetailsCard';

const EnsembleDetails = () => {
  const {token, checkTokenExpired} = useAuth();
  const [ensemble, setEnsemble] = useState('');
  const [selectedSegment, setSelectedSegment] = useState('Details');
  const [error, setError] = useState(false);
  const router = useRouter();
  const {id} = router.query;

  useEffect(() => {
    if (!checkTokenExpired() && id != null) {
      getEnsemble(id, token, setEnsemble, setError);
    }
  }, [id]);

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

  return (
    <div className="card container w-full md:w-11/12 w-11/12 max-w-7xl mt-2 mb-2">
      <Typography.Title level={2}>Ensemble Details ({ensemble.ensemble_id})</Typography.Title>
      <Divider />
      <Segmented options={['Details', 'Resources']} value={selectedSegment}
        onChange={(e) => setSelectedSegment(e)} size="large" block/>
      <Divider />
      {
        selectedSegment === 'Details' && ensemble &&
        <EnsembleDetailsCard ensemble={ensemble} reloadFunction={reloadEnsemble}/>
      }
      {
        selectedSegment === 'Resources' && ensemble && (
          <>
            <div>
              <Typography.Title level={3}>Resources</Typography.Title>
              {
                <ResourceTable resources={ensemble.resources} hasActions />
              }
            </div>
          </>)
      }
    </div>
  );
};

export default EnsembleDetails;
