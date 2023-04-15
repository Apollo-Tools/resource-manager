import {Divider, Typography} from 'antd';
import {useAuth} from '../../lib/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteVPC, listVPCs} from '../../lib/VPCService';
import VPCTable from './VPCTable';
import NewVPCForm from './NewVPCForm';

const VPCCard = () => {
  const {token, checkTokenExpired} = useAuth();
  const [isFinished, setFinished] = useState(false);
  const [vpcs, setVPCs] = useState([]);
  const [excludeRegions, setExcludeRegions] = useState([]);
  const [error, setError] = useState(false);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listVPCs(token, setVPCs, setError);
    }
  }, []);

  useEffect(() => {
    if (vpcs) {
      setExcludeRegions(() => vpcs.map((vpc) => vpc.region.region_id));
    }
  }, [vpcs]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      listVPCs(token, setVPCs, setError);
    }
  }, [isFinished]);

  // TODO: improve error handling
  useEffect(() => {
    if (error) {
      console.log('Unexpected error');
      setError(false);
    }
  }, [error]);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteVPC(id, token, setError)
          .then((result) => {
            if (result) {
              setVPCs((prevVPCs) => prevVPCs.filter((vpc) => vpc.vpc_id !== id));
            }
          });
    }
  };

  return <>
    <Typography.Title level={2}>Virtual Private Clouds</Typography.Title>
    <NewVPCForm excludeRegions={excludeRegions} setFinished={setFinished}/>
    <Divider />
    <VPCTable vpcs={vpcs} hasActions={true} onDelete={onClickDelete} />
  </>;
};


export default VPCCard;
