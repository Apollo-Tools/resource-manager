import {useEffect} from 'react';
import PropTypes from 'prop-types';
import {Space} from 'antd';
import SloDisplay from '../misc/slos/SloDisplay';
import DateFormatter from '../misc/DateFormatter';
import TextDataDisplay from '../misc/TextDataDisplay';
import DataDisplay from '../misc/DataDisplay';

const EnsembleDetailsCard = ({ensemble, setError}) => {
  useEffect(() => {
    if (ensemble != null && Object.hasOwn(ensemble, 'runtime')) {
    }
  }, [ensemble]);

  useEffect(() => {
    if (ensemble != null) {
    }
  }, [ensemble]);

  return (<Space direction="vertical" className="w-full" size='large'>
    <TextDataDisplay value={ensemble.name} label="Name" />
    <DataDisplay label="Service Level Objectives">
      <SloDisplay slos={ensemble.slos} setError={setError} />
    </DataDisplay>
    <TextDataDisplay value={<DateFormatter dateTimestamp={ensemble.created_at} includeTime/>} label="Created at" />
    <TextDataDisplay value={<DateFormatter dateTimestamp={ensemble.updated_at} includeTime/>} label="Updated at" />
  </Space>
  );
};

EnsembleDetailsCard.propTypes = {
  ensemble: PropTypes.object.isRequired,
  setError: PropTypes.func.isRequired,
};

export default EnsembleDetailsCard;
