import {Divider, Typography} from 'antd';
import {useAuth} from '../../lib/misc/AuthenticationProvider';
import {useEffect, useState} from 'react';
import {deleteVPC, listVPCs} from '../../lib/api/VPCService';
import VPCTable from './VPCTable';
import NewVPCForm from './NewVPCForm';
import PropTypes from 'prop-types';

const VPCCard = ({setError}) => {
  const {token, checkTokenExpired} = useAuth();
  const [isFinished, setFinished] = useState(false);
  const [isLoading, setLoading] = useState(false);
  const [vpcs, setVPCs] = useState([]);
  const [excludeRegions, setExcludeRegions] = useState([]);

  useEffect(() => {
    if (!checkTokenExpired()) {
      void listVPCs(token, setVPCs, setLoading, setError);
    }
  }, []);

  useEffect(() => {
    if (vpcs) {
      setExcludeRegions(() => vpcs.map((vpc) => vpc.region.region_id));
    }
  }, [vpcs]);

  useEffect(() => {
    if (!checkTokenExpired() && isFinished) {
      void listVPCs(token, setVPCs, setLoading, setError);
    }
    setFinished(false);
  }, [isFinished]);

  const onClickDelete = (id) => {
    if (!checkTokenExpired()) {
      deleteVPC(id, token, setLoading, setError)
          .then((result) => {
            if (result) {
              setVPCs((prevVPCs) => prevVPCs.filter((vpc) => vpc.vpc_id !== id));
            }
          });
    }
  };

  return <>
    <Typography.Title level={2}>Virtual Private Clouds</Typography.Title>
    <NewVPCForm excludeRegions={excludeRegions} setFinished={setFinished} setError={setError} isLoading={isLoading}/>
    <Divider />
    <VPCTable vpcs={vpcs} hasActions={true} onDelete={onClickDelete} isLoading={isLoading}/>
  </>;
};

VPCCard.propTypes = {
  setError: PropTypes.func.isRequired,
};

export default VPCCard;
